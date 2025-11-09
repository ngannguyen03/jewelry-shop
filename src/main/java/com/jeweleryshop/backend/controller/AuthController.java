package com.jeweleryshop.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jeweleryshop.backend.dto.AuthResponse;
import com.jeweleryshop.backend.dto.OtpVerifyRequest;
import com.jeweleryshop.backend.dto.RefreshTokenRequest;
import com.jeweleryshop.backend.dto.UserLoginRequest;
import com.jeweleryshop.backend.dto.UserRegistrationRequest;
import com.jeweleryshop.backend.dto.UserResponse;
import com.jeweleryshop.backend.entity.RefreshToken;
import com.jeweleryshop.backend.exception.AppException;
import com.jeweleryshop.backend.service.AuthService;
import com.jeweleryshop.backend.service.CartService;
import com.jeweleryshop.backend.service.RefreshTokenService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final CartService cartService;

    public AuthController(
            AuthService authService,
            RefreshTokenService refreshTokenService,
            CartService cartService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.cartService = cartService;
    }

    // ============================================================
    // 🧩 1️⃣ Đăng ký tài khoản
    // ============================================================
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponse user = authService.register(request);
        cartService.createCartIfNotExists(user.getId());
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    // ============================================================
    // 🔐 2️⃣ Đăng nhập (gửi OTP nếu bật 2FA)
    // ============================================================
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@Valid @RequestBody UserLoginRequest request) {
        AuthResponse response = authService.login(request);

        // Nếu user bật 2FA → chỉ trả thông báo yêu cầu OTP, không tạo cart
        if ("OTP_REQUIRED".equals(response.getMessage())) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        }

        // ✅ Tự động tạo giỏ hàng nếu user chưa có
        if (response.getUser() != null && response.getUser().getId() != null) {
            cartService.createCartIfNotExists(response.getUser().getId());
        }

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 🔑 3️⃣ Xác minh OTP để đăng nhập
    // ============================================================
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@RequestBody OtpVerifyRequest request) {
        AuthResponse response = authService.verifyOtp(request.getUsername(), request.getOtp());

        // ✅ Tự tạo giỏ hàng sau khi xác thực thành công
        if (response.getUser() != null && response.getUser().getId() != null) {
            cartService.createCartIfNotExists(response.getUser().getId());
        }

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // ♻️ 4️⃣ Làm mới Access Token
    // ============================================================
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return refreshTokenService.findByToken(request.getRefreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> ResponseEntity.ok(authService.refreshAccessToken(request.getRefreshToken(), user)))
                .orElseThrow(() -> new AppException("Refresh token không tồn tại hoặc đã hết hạn!"));
    }

    // ============================================================
    // 🚪 5️⃣ Đăng xuất
    // ============================================================
    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.ok("Đăng xuất thành công!");
    }
}
