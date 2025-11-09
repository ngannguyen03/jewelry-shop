package com.jeweleryshop.backend.mapper;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.jeweleryshop.backend.dto.RoleDTO;
import com.jeweleryshop.backend.dto.UserResponse;
import com.jeweleryshop.backend.entity.User;

@Component
public class UserMapper {

    /**
     * ✅ Map từ Entity → DTO đầy đủ (gồm mật khẩu gốc & mã hóa)
     */
    public UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }

        String mainRole = user.getRoles() != null && !user.getRoles().isEmpty()
                ? user.getRoles().iterator().next().getName()
                : "ROLE_USER";

        var roleDTOs = user.getRoles() != null
                ? user.getRoles().stream()
                        .map(role -> new RoleDTO(role.getId(), role.getName()))
                        .collect(Collectors.toSet())
                : null;

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(), // mã hóa (hash)
                user.getPlainPassword(), // ✅ mật khẩu gốc
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
