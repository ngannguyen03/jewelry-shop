package com.jeweleryshop.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.jeweleryshop.backend.dto.UserRegistrationRequest;
import com.jeweleryshop.backend.dto.UserResponse;
import com.jeweleryshop.backend.dto.UserUpdateRequest;
import com.jeweleryshop.backend.entity.Role;
import com.jeweleryshop.backend.entity.User;
import com.jeweleryshop.backend.exception.ResourceNotFoundException;
import com.jeweleryshop.backend.exception.UserAlreadyExistsException;
import com.jeweleryshop.backend.mapper.UserMapper;
import com.jeweleryshop.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleService roleService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserRegistrationRequest regRequest;
    private UserUpdateRequest updateRequest;
    private Role role;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setUsername("tuan");
        user.setEmail("tuan@example.com");
        user.setPassword("123");
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());

        role = new Role();
        role.setId(1L);
        role.setName("ROLE_USER");

        regRequest = new UserRegistrationRequest();
        regRequest.setUsername("tuan");
        regRequest.setEmail("tuan@example.com");
        regRequest.setPassword("123");
        regRequest.setFirstName("Nguyen");
        regRequest.setLastName("Tuan");
        regRequest.setPhoneNumber("0901234567");
        regRequest.setRoles(Set.of("ROLE_USER"));

        updateRequest = new UserUpdateRequest();
        updateRequest.setUsername("newtuan");
        updateRequest.setEmail("new@example.com");
        updateRequest.setFirstName("Anh");
        updateRequest.setLastName("Tuan");
        updateRequest.setPhoneNumber("0987654321");
    }

    // =====================================================
    // 🧩 CREATE USER
    // =====================================================
    @Test
    void testCreateUser_Success() {
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(roleService.findByNameOptional("ROLE_USER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("123")).thenReturn("encoded123");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(userMapper.toUserResponse(any(User.class))).thenReturn(new UserResponse());

        UserResponse res = userService.createUser(regRequest);

        assertNotNull(res);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreateUser_UsernameExists_ShouldThrow() {
        when(userRepository.existsByUsername("tuan")).thenReturn(true);
        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(regRequest));
    }

    @Test
    void testCreateUser_EmailExists_ShouldThrow() {
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(true);
        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(regRequest));
    }

    @Test
    void testCreateUser_InvalidRole_ShouldThrow() {
        regRequest.setRoles(Set.of("INVALID"));
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(roleService.findByNameOptional("INVALID")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.createUser(regRequest));
    }

    // =====================================================
    // 🧩 GET USERS
    // =====================================================
    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toUserResponse(any(User.class))).thenReturn(new UserResponse());
        List<UserResponse> list = userService.getAllUsers();
        assertEquals(1, list.size());
    }

    @Test
    void testGetUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(any(User.class))).thenReturn(new UserResponse());
        assertNotNull(userService.getUserById(1L));
    }

    @Test
    void testGetUserById_NotFound_ShouldThrow() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(1L));
    }

    @Test
    void testFindByUsername_Success() {
        when(userRepository.findByUsername("tuan")).thenReturn(Optional.of(user));
        assertEquals(user, userService.findByUsername("tuan"));
    }

    @Test
    void testFindByUsername_NotFound_ShouldThrow() {
        when(userRepository.findByUsername("tuan")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.findByUsername("tuan"));
    }

    // =====================================================
    // 🧩 UPDATE USER (ADMIN)
    // =====================================================
    @Test
    void testUpdateUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(userMapper.toUserResponse(any(User.class))).thenReturn(new UserResponse());
        UserResponse res = userService.updateUser(1L, updateRequest);
        assertNotNull(res);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateUser_UsernameAlreadyExists_ShouldThrow() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("newtuan")).thenReturn(true);
        assertThrows(UserAlreadyExistsException.class, () -> userService.updateUser(1L, updateRequest));
    }

    @Test
    void testUpdateUser_EmailAlreadyExists_ShouldThrow() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        updateRequest.setUsername(null);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);
        assertThrows(UserAlreadyExistsException.class, () -> userService.updateUser(1L, updateRequest));
    }

    @Test
    void testUpdateUser_RoleNotFound_ShouldThrow() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        updateRequest.setRoles(Set.of("ROLE_FAKE"));
        when(roleService.findByNameOptional("ROLE_FAKE")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(1L, updateRequest));
    }

    // =====================================================
    // 🧩 DELETE USER
    // =====================================================
    @Test
    void testDeleteUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        userService.deleteUser(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void testDeleteUser_NotFound_ShouldThrow() {
        when(userRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(1L));
    }

    // =====================================================
    // 🧩 UPDATE STATUS
    // =====================================================
    @Test
    void testUpdateUserStatus_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        userService.updateUserStatus(1L, false);
        verify(userRepository).save(any(User.class));
        assertFalse(user.getEnabled());
    }

    @Test
    void testUpdateUserStatus_NotFound_ShouldThrow() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUserStatus(1L, true));
    }

    // =====================================================
    // 🧩 USER SELF UPDATE
    // =====================================================
    @Test
    void testGetUserByUsername_Success() {
        when(userRepository.findByUsername("tuan")).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(any(User.class))).thenReturn(new UserResponse());
        assertNotNull(userService.getUserByUsername("tuan"));
    }

    @Test
    void testGetUserByUsername_NotFound_ShouldThrow() {
        when(userRepository.findByUsername("tuan")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserByUsername("tuan"));
    }

    @Test
    void testUpdateCurrentUser_Success() {
        when(userRepository.findByUsername("tuan")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(userMapper.toUserResponse(any(User.class))).thenReturn(new UserResponse());
        assertNotNull(userService.updateCurrentUser("tuan", updateRequest));
    }

    @Test
    void testUpdateCurrentUser_EmailExists_ShouldThrow() {
        when(userRepository.findByUsername("tuan")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);
        assertThrows(UserAlreadyExistsException.class, () -> userService.updateCurrentUser("tuan", updateRequest));
    }

    @Test
    void testUpdateCurrentUser_NotFound_ShouldThrow() {
        when(userRepository.findByUsername("tuan")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.updateCurrentUser("tuan", updateRequest));
    }
}
