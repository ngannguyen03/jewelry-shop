package com.jeweleryshop.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class UserLoginRequest {

    // Thay thế email bằng username
    @NotBlank(message = "Tên đăng nhập không được để trống")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    // Cần thay đổi getter/setter từ getEmail/setEmail sang getUsername/setUsername
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
