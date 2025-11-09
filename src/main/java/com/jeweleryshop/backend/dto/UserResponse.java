package com.jeweleryshop.backend.dto;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * ✅ Dữ liệu phản hồi của người dùng gửi về Frontend. Hiển thị đầy đủ thông tin
 * cá nhân, vai trò, mật khẩu (gốc và mã hóa).
 */
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String password;        // Mật khẩu mã hóa (đăng nhập)
    private String plainPassword;   // ✅ Mật khẩu gốc (hiển thị rõ trên admin)
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean enabled;
    private Set<RoleDTO> roles;
    private String mainRole;

    public UserResponse() {
    }

    public UserResponse(Long id, String username, String email,
            String password, String plainPassword,
            String firstName, String lastName, String phoneNumber,
            LocalDateTime createdAt, LocalDateTime updatedAt,
            Boolean enabled, Set<RoleDTO> roles, String mainRole) {

        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.plainPassword = plainPassword;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.enabled = enabled;
        this.roles = roles;
        this.mainRole = mainRole;
    }

    // --- GETTERS & SETTERS ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPlainPassword() {
        return plainPassword;
    }

    public void setPlainPassword(String plainPassword) {
        this.plainPassword = plainPassword;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Set<RoleDTO> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleDTO> roles) {
        this.roles = roles;
    }

    public String getMainRole() {
        return mainRole;
    }

    public void setMainRole(String mainRole) {
        this.mainRole = mainRole;
    }
}
