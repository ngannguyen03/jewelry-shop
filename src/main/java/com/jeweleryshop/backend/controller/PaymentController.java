package com.jeweleryshop.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.jeweleryshop.backend.dto.PaymentDTO;
import com.jeweleryshop.backend.service.PaymentService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${frontend.url}")
    private String frontendUrl;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create")
    public ResponseEntity<PaymentDTO.VnPayResponse> createPayment(HttpServletRequest request) {
        return new ResponseEntity<>(paymentService.createVnPayPayment(request), HttpStatus.OK);
    }

    @GetMapping("/vnpay-return")
    public RedirectView vnPayReturn(HttpServletRequest request) {
        String status = paymentService.processVnPayReturn(request);
        String orderId = request.getParameter("vnp_OrderInfo");

        String redirectUrl = frontendUrl + "/payment/result?orderId=" + orderId;

        if ("success".equals(status) || "order_already_confirmed".equals(status)) {
            return new RedirectView(redirectUrl + "&status=success");
        } else if ("invalid_signature".equals(status)) {
            return new RedirectView(redirectUrl + "&status=error&message=invalid_signature");
        } else {
            return new RedirectView(redirectUrl + "&status=failed");
        }
    }
}
