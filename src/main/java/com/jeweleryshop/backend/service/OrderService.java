package com.jeweleryshop.backend.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeweleryshop.backend.dto.AddressDTO;
import com.jeweleryshop.backend.dto.CreateOrderRequestDTO;
import com.jeweleryshop.backend.dto.OrderDetailResponseDTO;
import com.jeweleryshop.backend.entity.Address;
import com.jeweleryshop.backend.entity.Cart;
import com.jeweleryshop.backend.entity.CartItem;
import com.jeweleryshop.backend.entity.Inventory;
import com.jeweleryshop.backend.entity.Order;
import com.jeweleryshop.backend.entity.OrderDetail;
import com.jeweleryshop.backend.entity.OrderStatus;
import com.jeweleryshop.backend.entity.Product;
import com.jeweleryshop.backend.entity.ProductImage;
import com.jeweleryshop.backend.entity.ProductVariant;
import com.jeweleryshop.backend.entity.Promotion;
import com.jeweleryshop.backend.entity.User;
import com.jeweleryshop.backend.exception.AppException;
import com.jeweleryshop.backend.exception.ResourceNotFoundException;
import com.jeweleryshop.backend.repository.AddressRepository;
import com.jeweleryshop.backend.repository.CartRepository;
import com.jeweleryshop.backend.repository.OrderRepository;
import com.jeweleryshop.backend.repository.ProductVariantRepository;
import com.jeweleryshop.backend.repository.UserRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final ProductVariantRepository variantRepository;
    private final PromotionService promotionService;

    public OrderService(
            OrderRepository orderRepository,
            UserRepository userRepository,
            CartRepository cartRepository,
            AddressRepository addressRepository,
            ProductVariantRepository variantRepository,
            PromotionService promotionService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.addressRepository = addressRepository;
        this.variantRepository = variantRepository;
        this.promotionService = promotionService;
    }

    // ============================
    // 🧾 TẠO ĐƠN HÀNG
    // ============================
    @Transactional
    public OrderDetailResponseDTO createOrder(CreateOrderRequestDTO requestDTO) {
        User currentUser = getCurrentUser();

        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + currentUser.getUsername()));

        if (cart.getItems().isEmpty()) {
            throw new AppException("Cannot create an order with an empty cart.");
        }

        Address shippingAddress = addressRepository.findById(requestDTO.getShippingAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + requestDTO.getShippingAddressId()));

        if (!shippingAddress.getUser().getId().equals(currentUser.getId())) {
            throw new AppException("Shipping address does not belong to the current user.");
        }

        // ====== Khởi tạo đơn hàng ======
        Order order = new Order();
        order.setUser(currentUser);
        order.setShippingAddress(shippingAddress);
        order.setOrderNumber(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal totalPrice = BigDecimal.ZERO;

        // ====== Tạo chi tiết đơn hàng từ giỏ hàng ======
        for (CartItem cartItem : cart.getItems()) {
            ProductVariant variant = variantRepository.findWithLockingById(cartItem.getVariant().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product Variant not found."));

            Product product = variant.getProduct();
            Inventory inventory = variant.getInventory();

            if (inventory == null || inventory.getQuantity() < cartItem.getQuantity()) {
                throw new AppException("Not enough stock for product: " + product.getName());
            }

            // Cập nhật tồn kho
            inventory.setQuantity(inventory.getQuantity() - cartItem.getQuantity());

            OrderDetail detail = new OrderDetail();
            detail.setVariant(variant);
            detail.setQuantity(cartItem.getQuantity());

            // ✅ Tính giá hợp lệ và tránh null
            BigDecimal basePrice = product.getBasePrice() != null ? product.getBasePrice() : BigDecimal.ZERO;
            BigDecimal modifier = variant.getPriceModifier() != null ? variant.getPriceModifier() : BigDecimal.ZERO;

            BigDecimal effectivePrice = product.getDiscountPrice() != null
                    ? product.getDiscountPrice()
                    : basePrice.add(modifier);

            detail.setPriceAtPurchase(effectivePrice);
            order.addOrderDetail(detail);

            totalPrice = totalPrice.add(effectivePrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        // ==========================
        // 💰 Tính tổng đơn hàng cuối cùng
        // ==========================
        BigDecimal shippingFee = BigDecimal.valueOf(30000); // mặc định 30k
        BigDecimal discountAmount = BigDecimal.ZERO;
        String discountCode = requestDTO.getDiscountCode();

        if (discountCode != null && discountCode.equalsIgnoreCase("SALE10")) {
            discountAmount = totalPrice.multiply(BigDecimal.valueOf(0.1)); // giảm 10%
        }

        BigDecimal finalTotal = totalPrice.subtract(discountAmount).add(shippingFee);

        order.setTotalAmount(totalPrice);
        order.setShippingFee(shippingFee);
        order.setDiscountCode(discountCode);
        order.setDiscountAmount(discountAmount);
        order.setFinalTotal(finalTotal);

        // Áp dụng khuyến mãi (nếu có)
        Promotion appliedPromotion = cart.getAppliedPromotion();
        if (appliedPromotion != null) {
            promotionService.incrementUsage(appliedPromotion.getCode());
        }

        // Lưu đơn hàng
        Order savedOrder = orderRepository.save(order);

        // Xóa giỏ hàng sau khi đặt
        cart.getItems().clear();
        cartRepository.save(cart);

        return convertToDetailDTO(savedOrder);
    }

    // ============================
    // 👤 NGƯỜI DÙNG XEM ĐƠN HÀNG
    // ============================
    @Transactional(readOnly = true)
    public List<OrderDetailResponseDTO> getOrdersForCurrentUser() {
        User currentUser = getCurrentUser();
        List<Order> orders = orderRepository.findByUserIdOrderByOrderDateDesc(currentUser.getId());
        return orders.stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderDetailResponseDTO getOrderDetails(Long orderId) {
        User currentUser = getCurrentUser();
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new AppException("You are not authorized to view this order.");
        }

        return convertToDetailDTO(order);
    }

    // ============================
    // 🧩 ADMIN
    // ============================
    @Transactional(readOnly = true)
    public Page<OrderDetailResponseDTO> getAllOrders(Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAll(pageable);
        return orderPage.map(this::convertToDetailDTO);
    }

    @Transactional
    public OrderDetailResponseDTO updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Hoàn kho nếu hủy đơn
        if (newStatus == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.CANCELLED) {
            for (OrderDetail detail : order.getOrderDetails()) {
                ProductVariant variant = detail.getVariant();
                Inventory inventory = variant.getInventory();
                if (inventory != null) {
                    inventory.setQuantity(inventory.getQuantity() + detail.getQuantity());
                }
            }
        }

        order.setStatus(newStatus);
        return convertToDetailDTO(orderRepository.save(order));
    }

    // ============================
    // 🔄 CHUYỂN ENTITY → DTO
    // ============================
    private OrderDetailResponseDTO convertToDetailDTO(Order order) {
        Address shippingAddress = order.getShippingAddress();
        AddressDTO addressDTO = null;

        if (shippingAddress != null) {
            addressDTO = new AddressDTO(
                    shippingAddress.getId(),
                    shippingAddress.getFullName(),
                    shippingAddress.getPhoneNumber(),
                    shippingAddress.getStreetAddress(),
                    shippingAddress.getCity(),
                    shippingAddress.getDistrict(),
                    shippingAddress.getWard()
            );
        }

        List<OrderDetailResponseDTO.OrderItemDTO> itemDTOs = order.getOrderDetails().stream().map(detail -> {
            ProductVariant variant = detail.getVariant();
            Product product = variant.getProduct();
            String variantInfo = String.format("Size: %s, Material: %s",
                    variant.getSize() != null ? variant.getSize() : "N/A",
                    variant.getMaterial() != null ? variant.getMaterial() : "N/A");

            String imageUrl = product.getImages().stream().findFirst().map(ProductImage::getImageUrl).orElse(null);

            return new OrderDetailResponseDTO.OrderItemDTO(
                    product.getId(),
                    variant.getId(),
                    product.getName(),
                    variantInfo,
                    imageUrl,
                    detail.getQuantity(),
                    detail.getPriceAtPurchase()
            );
        }).collect(Collectors.toList());

        OrderDetailResponseDTO dto = new OrderDetailResponseDTO(
                order.getId(),
                order.getOrderDate(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getDiscountAmount(),
                order.getFinalTotal(),
                addressDTO,
                itemDTOs
        );

        dto.setShippingFee(order.getShippingFee());
        dto.setDiscountCode(order.getDiscountCode());

        User user = order.getUser();
        if (user != null) {
            dto.setUserEmail(user.getEmail());
            dto.setUserName((user.getFirstName() != null ? user.getFirstName() : "")
                    + " "
                    + (user.getLastName() != null ? user.getLastName() : ""));
        } else {
            dto.setUserEmail("Ẩn danh");
            dto.setUserName("Ẩn danh");
        }

        dto.setTotalPrice(order.getFinalTotal());
        return dto;
    }

    // ============================
    // 🧍‍♂️ LẤY USER HIỆN TẠI
    // ============================
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }
}
