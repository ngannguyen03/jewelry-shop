package com.jeweleryshop.backend.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.jeweleryshop.backend.exception.AppException;

@Service
public class OtpService {

    private final Map<String, OtpData> otpStore = new ConcurrentHashMap<>();
    private static final int EXPIRATION_MINUTES = 5;

    // ✅ Sinh mã OTP 6 số và lưu tạm trong bộ nhớ
    public String generateOtp(String username) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStore.put(username, new OtpData(otp, LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES)));
        return otp;
    }

    // ✅ Xác minh mã OTP (so sánh và kiểm tra thời gian hết hạn)
    public void verifyOtp(String username, String inputOtp) {
        OtpData data = otpStore.get(username);
        if (data == null) {
            throw new AppException("OTP không tồn tại hoặc đã hết hạn!");
        }

        if (data.getExpiryTime().isBefore(LocalDateTime.now())) {
            otpStore.remove(username);
            throw new AppException("OTP đã hết hạn!");
        }

        if (!data.getOtp().equals(inputOtp)) {
            throw new AppException("Mã OTP không đúng!");
        }

        // ✅ Xác thực thành công → xóa khỏi store
        otpStore.remove(username);
    }

    // ✅ Inner class để lưu dữ liệu OTP
    private static class OtpData {

        private final String otp;
        private final LocalDateTime expiryTime;

        public OtpData(String otp, LocalDateTime expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }

        public String getOtp() {
            return otp;
        }

        public LocalDateTime getExpiryTime() {
            return expiryTime;
        }
    }
}
