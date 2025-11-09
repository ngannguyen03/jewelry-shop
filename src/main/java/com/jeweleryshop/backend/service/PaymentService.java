package com.jeweleryshop.backend.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeweleryshop.backend.config.VnPayConfig;
import com.jeweleryshop.backend.dto.PaymentDTO;
import com.jeweleryshop.backend.entity.Order;
import com.jeweleryshop.backend.entity.OrderStatus;
import com.jeweleryshop.backend.entity.PaymentStatus;
import com.jeweleryshop.backend.exception.ResourceNotFoundException;
import com.jeweleryshop.backend.repository.OrderRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class PaymentService {

    @Value("${vnpay.url}")
    private String vnp_PayUrl;
    @Value("${vnpay.return_url}")
    private String vnp_ReturnUrl;
    @Value("${vnpay.tmn_code}")
    private String vnp_TmnCode;
    @Value("${vnpay.hash_secret}")
    private String vnp_HashSecret;
    @Value("${vnpay.version}")
    private String vnp_Version;

    private final OrderRepository orderRepository;

    public PaymentService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Tạo URL thanh toán VNPay. Phương thức này nhận thông tin từ request, tạo
     * các tham số cần thiết, ký chúng bằng mã bí mật và trả về một URL để
     * chuyển hướng người dùng đến cổng thanh toán.
     *
     * @param request HttpServletRequest chứa các tham số như 'amount',
     * 'orderId'.
     * @return Đối tượng chứa URL thanh toán.
     */
    public PaymentDTO.VnPayResponse createVnPayPayment(HttpServletRequest request) {
        long amount = Integer.parseInt(request.getParameter("amount")) * 100L;
        // The order ID must be passed from the frontend to be used as vnp_OrderInfo
        String orderId = request.getParameter("orderId");

        String bankCode = request.getParameter("bankCode");

        String vnp_TxnRef = VnPayConfig.getRandomNumber(8);
        String vnp_IpAddr = VnPayConfig.getIpAddress(request);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");

        if (bankCode != null && !bankCode.isEmpty()) {
            vnp_Params.put("vnp_BankCode", bankCode);
        }
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", orderId); // Use orderId as vnp_OrderInfo for easy retrieval
        vnp_Params.put("vnp_OrderType", "other");

        String locate = request.getParameter("language");
        if (locate != null && !locate.isEmpty()) {
            vnp_Params.put("vnp_Locale", locate);
        } else {
            vnp_Params.put("vnp_Locale", "vn");
        }
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VnPayConfig.hmacSHA512(vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnp_PayUrl + "?" + queryUrl;

        return new PaymentDTO.VnPayResponse("00", "Successfully", paymentUrl);
    }

    /**
     * Xử lý callback từ VNPay sau khi người dùng hoàn tất thanh toán. Phương
     * thức này sẽ: 1. Xác minh tính toàn vẹn của dữ liệu trả về bằng cách kiểm
     * tra chữ ký (vnp_SecureHash). 2. Kiểm tra trạng thái thanh toán
     * (vnp_ResponseCode). 3. Cập nhật trạng thái đơn hàng và trạng thái thanh
     * toán trong database nếu thành công.
     *
     * @param request HttpServletRequest chứa các tham số do VNPay trả về.
     * @return Chuỗi kết quả ("success", "failed", "invalid_signature", ...).
     */
    @Transactional
    public String processVnPayReturn(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        if (fields.containsKey("vnp_SecureHashType")) {
            fields.remove("vnp_SecureHashType");
        }
        if (fields.containsKey("vnp_SecureHash")) {
            fields.remove("vnp_SecureHash");
        }

        // Sắp xếp các trường theo thứ tự alphabet để tạo chuỗi hash
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }

        // Tính toán lại chữ ký từ dữ liệu nhận được
        String calculatedHash = VnPayConfig.hmacSHA512(vnp_HashSecret, hashData.toString());

        // So sánh chữ ký tính toán với chữ ký VNPay gửi về
        if (!calculatedHash.equals(vnp_SecureHash)) {
            return "invalid_signature"; // Chữ ký không hợp lệ
        }

        String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
        String vnp_OrderInfo = request.getParameter("vnp_OrderInfo"); // Đây chính là orderId
        Long orderId = Long.parseLong(vnp_OrderInfo);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Nếu mã phản hồi là "00" (thành công)
        if ("00".equals(vnp_ResponseCode)) {
            // Chỉ cập nhật nếu đơn hàng đang ở trạng thái chờ xử lý để tránh xử lý lại
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.PROCESSING); // Chuyển trạng thái xử lý đơn hàng
                order.setPaymentStatus(PaymentStatus.PAID);
                orderRepository.save(order);
                return "success"; // Thanh toán thành công
            }
            return "order_already_confirmed"; // Đơn hàng đã được xác nhận trước đó
        } else {
            // Thanh toán thất bại, có thể cập nhật trạng thái đơn hàng thành FAILED hoặc CANCELLED
            return "failed"; // Thanh toán thất bại
        }
    }
}
