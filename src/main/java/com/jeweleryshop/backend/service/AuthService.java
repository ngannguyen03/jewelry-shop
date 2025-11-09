package com.jeweleryshop.backend.service;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeweleryshop.backend.dto.AuthResponse;
import com.jeweleryshop.backend.dto.RefreshTokenRequest;
import com.jeweleryshop.backend.dto.RoleDTO;
import com.jeweleryshop.backend.dto.UserLoginRequest;
import com.jeweleryshop.backend.dto.UserRegistrationRequest;
import com.jeweleryshop.backend.dto.UserResponse;
import com.jeweleryshop.backend.entity.RefreshToken;
import com.jeweleryshop.backend.entity.User;
import com.jeweleryshop.backend.exception.AppException;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final OtpService otpService;
    private final EmailService emailService;

    public AuthService(
            UserService userService,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            RefreshTokenService refreshTokenService,
            OtpService otpService,
            EmailService emailService) {

        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    // ============================================================
    // 🧩 1️⃣ Đăng ký tài khoản
    // ============================================================
    @Transactional
    public UserResponse register(UserRegistrationRequest request) {
        return userService.createUser(request);
    }

    // ============================================================
    // 🔐 2️⃣ Đăng nhập (có hỗ trợ 2FA bằng email OTP)
    // ============================================================
    @Transactional
    public AuthResponse login(UserLoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userService.findByUsername(request.getUsername());
        if (user == null) {
            throw new AppException("Không tìm thấy người dùng!");
        }

        // ⚠️ Nếu user bị khóa
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new AppException("Tài khoản của bạn đã bị khóa!");
        }

        // ✅ Nếu bật 2FA thì gửi mã OTP qua email
        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            String otp = otpService.generateOtp(user.getUsername());
            emailService.sendOtpEmail(user.getEmail(), otp);
            user.setLastOtpSentAt(LocalDateTime.now());

            return new AuthResponse(
                    null,
                    null,
                    "OTP_REQUIRED",
                    null
            );
        }

        // ✅ Nếu không bật 2FA → xử lý đăng nhập bình thường
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwtToken = jwtService.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername());

        return new AuthResponse(
                jwtToken,
                refreshToken.getToken(),
                "Đăng nhập thành công!",
                buildUserResponse(user)
        );
    }

    // ============================================================
    // 🔑 3️⃣ Xác minh OTP để hoàn tất đăng nhập
    // ============================================================
    @Transactional
    public AuthResponse verifyOtp(String username, String otpInput) {
        // ✅ Gọi trực tiếp — hàm sẽ ném lỗi nếu OTP sai hoặc hết hạn
        otpService.verifyOtp(username, otpInput);

        User user = userService.findByUsername(username);
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority(r.getName()))
                        .collect(Collectors.toList())
        );

        String jwtToken = jwtService.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername());

        return new AuthResponse(
                jwtToken,
                refreshToken.getToken(),
                "✅ Xác thực OTP thành công!",
                buildUserResponse(user)
        );
    }

    // ============================================================
    // 🚪 4️⃣ Đăng xuất
    // ============================================================
    public void logout(RefreshTokenRequest request) {
        refreshTokenService.deleteByToken(request.getRefreshToken());
    }

    // ============================================================
    // ♻️ 5️⃣ Làm mới Access Token
    // ============================================================
    public AuthResponse refreshAccessToken(String refreshTokenString, User user) {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority(r.getName()))
                        .collect(Collectors.toList())
        );

        String newAccessToken = jwtService.generateToken(userDetails);
        return new AuthResponse(
                newAccessToken,
                refreshTokenString,
                "Token refreshed successfully!",
                buildUserResponse(user)
        );
    }

    // ============================================================
    // 🧠 Helper: Xây dựng UserResponse
    // ============================================================
    private UserResponse buildUserResponse(User user) {
        var roleDTOs = user.getRoles().stream()
                .map(role -> new RoleDTO(role.getId(), role.getName()))
                .collect(Collectors.toSet());

        String mainRole = user.getRoles().stream()
                .findFirst()
                .map(r -> r.getName())
                .orElse("ROLE_USER");

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getPlainPassword(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getEnabled(),
                roleDTOs,
                mainRole
        );
    }
}
