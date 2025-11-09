package com.jeweleryshop.backend.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeweleryshop.backend.dto.UserRegistrationRequest;
import com.jeweleryshop.backend.dto.UserResponse;
import com.jeweleryshop.backend.dto.UserUpdateRequest;
import com.jeweleryshop.backend.entity.Role;
import com.jeweleryshop.backend.entity.User;
import com.jeweleryshop.backend.exception.ResourceNotFoundException;
import com.jeweleryshop.backend.exception.UserAlreadyExistsException;
import com.jeweleryshop.backend.mapper.UserMapper;
import com.jeweleryshop.backend.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository,
            RoleService roleService,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    // ============================================================
    // 🔹 1. Tạo mới người dùng
    // ============================================================
    @Transactional
    public UserResponse createUser(UserRegistrationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Tên đăng nhập đã tồn tại: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email đã tồn tại: " + request.getEmail());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPlainPassword(request.getPassword()); // chỉ Admin xem
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEnabled(true);

        // ✅ Vai trò mặc định
        Set<Role> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String roleName : request.getRoles()) {
                roleService.findByNameOptional(roleName.toUpperCase())
                        .ifPresentOrElse(roles::add,
                                () -> {
                                    throw new ResourceNotFoundException("Vai trò không tồn tại: " + roleName);
                                });
            }
        } else {
            roleService.findByNameOptional("ROLE_USER").ifPresent(roles::add);
        }
        user.setRoles(roles);

        User saved = userRepository.save(user);
        return userMapper.toUserResponse(saved);
    }

    // ============================================================
    // 🔹 2. Lấy tất cả người dùng
    // ============================================================
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    // ============================================================
    // 🔹 3. Lấy người dùng theo ID
    // ============================================================
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng ID: " + id));
        return userMapper.toUserResponse(user);
    }

    // ============================================================
    // 🔹 4. Lấy người dùng theo username
    // ============================================================
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + username));
    }

    // ============================================================
    // 🔹 5. Admin cập nhật thông tin người dùng
    // ============================================================
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));

        // ✅ Kiểm tra username trùng
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new UserAlreadyExistsException("Tên đăng nhập đã tồn tại: " + request.getUsername());
            }
            user.setUsername(request.getUsername());
        }

        // ✅ Kiểm tra email trùng
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new UserAlreadyExistsException("Email đã tồn tại: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        // ✅ Cập nhật các trường khác
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        // ✅ Cập nhật roles nếu có
        if (request.getRoles() != null) {
            Set<Role> newRoles = new HashSet<>();
            for (String roleName : request.getRoles()) {
                roleService.findByNameOptional(roleName)
                        .ifPresentOrElse(newRoles::add,
                                () -> {
                                    throw new ResourceNotFoundException("Vai trò không tồn tại: " + roleName);
                                });
            }
            user.setRoles(newRoles);
        }

        user.setUpdatedAt(LocalDateTime.now());
        User updated = userRepository.save(user);
        return userMapper.toUserResponse(updated);
    }

    // ============================================================
    // 🔹 6. Xóa người dùng
    // ============================================================
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng ID: " + id);
        }
        userRepository.deleteById(id);
    }

    // ============================================================
    // 🔹 7. Khóa / Mở khóa tài khoản
    // ============================================================
    @Transactional
    public void updateUserStatus(Long id, Boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng ID: " + id));
        user.setEnabled(enabled);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // ============================================================
    // 🧩 8. User tự cập nhật hồ sơ (/api/users/me)
    // ============================================================
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + username));
        return userMapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse updateCurrentUser(String username, UserUpdateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + username));

        // ✅ Kiểm tra email trùng với người khác
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new UserAlreadyExistsException("Email đã tồn tại: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        // ✅ Cập nhật các trường cho phép
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        // ❌ Không cập nhật username, password, role, enabled
        user.setUpdatedAt(LocalDateTime.now());

        User updated = userRepository.save(user);
        return userMapper.toUserResponse(updated);
    }
}
