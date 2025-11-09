package com.jeweleryshop.backend.dto;

import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * ✅ DTO dùng cho việc cập nhật thông tin người dùng. - Admin: cập nhật
 * username, email, tên, họ, số điện thoại, trạng thái, vai trò. - User (tự cập
 * nhật): chỉ được phép thay đổi email, tên, họ, số điện thoại.
 */
public class UserUpdateRequest {

    // ✅ ADMIN có thể cập nhật username, USER sẽ bị bỏ qua khi gửi /me
    @Size(min = 3, max = 50, message = "Tên đăng nhập phải có từ 3 đến 50 ký tự")
    private String username;

    @Email(message = "Email không hợp lệ")
    private String email;

    @Size(max = 50, message = "Tên không được vượt quá 50 ký tự")
    private String firstName;

    @Size(max = 50, message = "Họ không được vượt quá 50 ký tự")
    private String lastName;

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phoneNumber;

    /**
     * ✅ enabled = true → đang hoạt động ✅ enabled = false → bị khóa Chỉ ADMIN
     * có thể thay đổi.
     */
    private Boolean enabled;

    /**
     * ✅ Danh sách các vai trò (ROLE_USER, ROLE_ADMIN, ...) Chỉ ADMIN có thể
     * thay đổi.
     */
    private Set<String> roles;

    // =============================
    // 🧩 GETTER & SETTER
    // =============================
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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
