package com.jeweleryshop.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.jeweleryshop.backend.dto.AuthResponse;
import com.jeweleryshop.backend.dto.RefreshTokenRequest;
import com.jeweleryshop.backend.dto.UserLoginRequest;
import com.jeweleryshop.backend.dto.UserRegistrationRequest;
import com.jeweleryshop.backend.dto.UserResponse;
import com.jeweleryshop.backend.entity.RefreshToken;
import com.jeweleryshop.backend.entity.Role;
import com.jeweleryshop.backend.entity.User;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    private User user;
    private Role role;
    private RefreshToken refreshToken;
    private UserDetails userDetails;

    @BeforeEach
    void setup() {
        role = new Role();
        role.setId(1L);
        role.setName("ROLE_USER");

        user = new User();
        user.setId(1L);
        user.setUsername("tuan");
        user.setPassword("encoded");
        user.setPlainPassword("123");
        user.setEmail("tuan@example.com");
        user.setFirstName("Nguyen");
        user.setLastName("Tuan");
        user.setPhoneNumber("0901234567");
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setRoles(Set.of(role));

        refreshToken = new RefreshToken();
        refreshToken.setToken("refresh123");

        userDetails = new org.springframework.security.core.userdetails.User(
                "tuan", "encoded",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    // =========================================================
    // 🧩 REGISTER
    // =========================================================
    @Test
    void testRegister_Success() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        when(userService.createUser(request)).thenReturn(new UserResponse());
        UserResponse result = authService.register(request);
        assertNotNull(result);
        verify(userService).createUser(request);
    }

    // =========================================================
    // 🧩 LOGIN
    // =========================================================
    @Test
    void testLogin_Success() {
        UserLoginRequest request = new UserLoginRequest();
        request.setUsername("tuan");
        request.setPassword("123");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(userService.findByUsername("tuan")).thenReturn(user);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt123");
        when(refreshTokenService.createRefreshToken("tuan")).thenReturn(refreshToken);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt123", response.getAccessToken());
        assertEquals("refresh123", response.getRefreshToken());
        assertEquals("Đăng nhập thành công!", response.getMessage());
        verify(authenticationManager).authenticate(any());
        verify(userService).findByUsername("tuan");
    }

    // =========================================================
    // 🧩 LOGOUT
    // =========================================================
    @Test
    void testLogout_Success() {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("abc123");

        authService.logout(req);
        verify(refreshTokenService).deleteByToken("abc123");
    }

    // =========================================================
    // 🧩 REFRESH ACCESS TOKEN
    // =========================================================
    @Test
    void testRefreshAccessToken_Success() {
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("newJWT123");

        AuthResponse res = authService.refreshAccessToken("refresh123", user);

        assertNotNull(res);
        assertEquals("newJWT123", res.getAccessToken());
        assertEquals("refresh123", res.getRefreshToken());
        assertEquals("Token refreshed successfully!", res.getMessage());
        assertEquals("tuan", res.getUser().getUsername());
        assertEquals("ROLE_USER", res.getUser().getMainRole());
    }
}
