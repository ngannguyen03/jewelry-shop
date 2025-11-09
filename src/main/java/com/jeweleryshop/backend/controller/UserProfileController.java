package com.jeweleryshop.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.jeweleryshop.backend.dto.UserResponse;
import com.jeweleryshop.backend.dto.UserUpdateRequest;
import com.jeweleryshop.backend.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    private final UserService userService;

    public UserProfileController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 🔹 Lấy thông tin người dùng hiện tại
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        UserResponse user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    /**
     * 🔹 Cập nhật thông tin người dùng hiện tại
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            Authentication authentication,
            @Valid @RequestBody UserUpdateRequest request) {

        String username = authentication.getName();
        UserResponse updated = userService.updateCurrentUser(username, request);
        return ResponseEntity.ok(updated);
    }
}
