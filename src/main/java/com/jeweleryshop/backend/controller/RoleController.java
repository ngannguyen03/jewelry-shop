package com.jeweleryshop.backend.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jeweleryshop.backend.dto.RoleDTO;
import com.jeweleryshop.backend.service.RoleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/roles")
@PreAuthorize("hasRole('ADMIN')") // Chỉ người dùng có vai trò 'ADMIN' mới có thể truy cập các endpoint này.
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * Lấy danh sách tất cả các vai trò trong hệ thống.
     *
     * @return ResponseEntity chứa danh sách RoleDTO.
     */
    @GetMapping
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        List<RoleDTO> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    /**
     * Lấy thông tin một vai trò theo ID.
     *
     * @param id ID của vai trò cần lấy.
     * @return ResponseEntity chứa thông tin RoleDTO.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable Long id) {
        RoleDTO role = roleService.getRoleById(id);
        return ResponseEntity.ok(role);
    }

    /**
     * Tạo một vai trò mới. Tên vai trò sẽ tự động được chuyển thành chữ hoa (ví
     * dụ: "admin" -> "ADMIN").
     *
     * @param roleDTO Dữ liệu của vai trò mới.
     * @return ResponseEntity 201 Created với thông tin vai trò đã được tạo.
     */
    @PostMapping
    public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody RoleDTO roleDTO) {
        RoleDTO createdRole = roleService.createRole(roleDTO);
        return ResponseEntity.created(URI.create("/api/admin/roles/" + createdRole.getId())).body(createdRole);
    }

    /**
     * Cập nhật thông tin của một vai trò đã tồn tại.
     *
     * @param id ID của vai trò cần cập nhật.
     * @param roleDTO Dữ liệu cập nhật.
     * @return ResponseEntity chứa thông tin vai trò sau khi đã cập nhật.
     */
    @PutMapping("/{id}")
    public ResponseEntity<RoleDTO> updateRole(@PathVariable Long id, @Valid @RequestBody RoleDTO roleDTO) {
        RoleDTO updatedRole = roleService.updateRole(id, roleDTO);
        return ResponseEntity.ok(updatedRole);
    }

    /**
     * Xóa một vai trò khỏi hệ thống. Lưu ý: Cần cẩn thận khi xóa vai trò đang
     * được gán cho người dùng.
     *
     * @param id ID của vai trò cần xóa.
     * @return ResponseEntity 204 No Content nếu xóa thành công.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
