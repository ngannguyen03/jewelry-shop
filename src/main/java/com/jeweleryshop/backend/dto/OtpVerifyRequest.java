package com.jeweleryshop.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO dùng để xác minh mã OTP trong bước 2 của đăng nhập 2FA.
 */
public class OtpVerifyRequest {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    private String username;

    @NotBlank(message = "Mã OTP không được để trống")
    private String otp;

    public OtpVerifyRequest() {
    }

    public OtpVerifyRequest(String username, String otp) {
        this.username = username;
        this.otp = otp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
