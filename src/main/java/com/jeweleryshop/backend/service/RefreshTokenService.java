package com.jeweleryshop.backend.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeweleryshop.backend.entity.RefreshToken;
import com.jeweleryshop.backend.entity.User;
import com.jeweleryshop.backend.exception.AppException;
import com.jeweleryshop.backend.repository.RefreshTokenRepository;
import com.jeweleryshop.backend.repository.UserRepository;

@Service
public class RefreshTokenService {

    @Value("${app.jwt.refresh-token.expiration}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    // ✅ Hàm tạo refresh token mới (đã fix lỗi duplicate)
    @Transactional
    public RefreshToken createRefreshToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("User not found"));

        // 🔹 Xóa token cũ nếu tồn tại (tránh lỗi trùng khóa)
        refreshTokenRepository.deleteByUserId(user.getId());

        // 🔹 Tạo token mới
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));

        // 🔹 Lưu lại vào DB
        return refreshTokenRepository.saveAndFlush(refreshToken);
    }

    // ✅ Kiểm tra token có còn hạn hay không
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new AppException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    // ✅ Xóa token theo chuỗi token
    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }

    // ✅ Tìm theo token
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
}
