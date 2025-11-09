package com.jeweleryshop.backend.dto;

public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String message;
    private UserResponse user; // <--- Đã thêm User DTO

    public AuthResponse() {
    }

    public AuthResponse(String accessToken, String refreshToken, String message, UserResponse user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.message = message;
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }
}
