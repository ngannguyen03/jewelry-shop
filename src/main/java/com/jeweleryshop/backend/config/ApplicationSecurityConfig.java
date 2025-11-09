package com.jeweleryshop.backend.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.jeweleryshop.backend.security.JwtAuthenticationFilter;

/**
 * ✅ Cấu hình bảo mật Spring Security + JWT cho hệ thống
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Cho phép dùng @PreAuthorize, @PostAuthorize, ...
public class ApplicationSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    public ApplicationSecurityConfig(
            JwtAuthenticationFilter jwtAuthFilter,
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    /**
     * ⚙️ Cấu hình SecurityFilterChain - trung tâm của Spring Security
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 🚫 Vô hiệu hóa CSRF (vì ta dùng JWT, không dùng session)
                .csrf(AbstractHttpConfigurer::disable)
                // 🌐 Cho phép CORS (React FE truy cập)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // ⚠️ Cấu hình xử lý khi bị 401 Unauthorized
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                // 🧩 Phân quyền endpoint
                .authorizeHttpRequests(auth -> auth
                // ✅ Public routes (không cần JWT)
                .requestMatchers("/", "/favicon.ico").permitAll()
                .requestMatchers("/images/**", "/uploads/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/banners/**").permitAll()
                .requestMatchers("/api/products/**").permitAll()
                .requestMatchers("/api/categories/**").permitAll()
                .requestMatchers("/api/variants/*/reviews").permitAll()
                // ✅ Swagger (public)
                .requestMatchers(
                        "/v2/api-docs", "/v3/api-docs", "/v3/api-docs/**",
                        "/swagger-resources", "/swagger-resources/**",
                        "/configuration/ui", "/configuration/security",
                        "/swagger-ui/**", "/webjars/**", "/swagger-ui.html"
                ).permitAll()
                // 👤 USER & ADMIN được phép truy cập
                .requestMatchers("/api/cart/**").hasAnyRole("USER", "ADMIN")
                // 🧩 ✅ Cho phép USER & ADMIN xem / chỉnh sửa hồ sơ
                .requestMatchers("/api/users/**").hasAnyRole("USER", "ADMIN")
                // 👑 ADMIN route riêng
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // 🔒 Các route khác yêu cầu xác thực JWT
                .anyRequest().authenticated()
                )
                // ⚙️ Dùng Stateless session (chỉ dựa trên JWT)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 🔧 Gắn Filter JWT vào trước UsernamePasswordAuthenticationFilter
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 🌍 Cấu hình CORS cho React frontend (localhost & Vite)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:3001",
                "http://localhost:5173" // ⚡ Cho môi trường Vite dev server
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * 🧩 Cấu hình AuthenticationProvider (dùng UserDetailsService +
     * PasswordEncoder)
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * ⚙️ Cấu hình AuthenticationManager cho quá trình login
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
