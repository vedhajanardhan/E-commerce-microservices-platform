package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.request.LoginRequest;
import com.ecommerce.auth.dto.request.RegisterRequest;
import com.ecommerce.auth.dto.response.AuthResponse;
import com.ecommerce.auth.entity.ERole;
import com.ecommerce.auth.entity.RefreshToken;
import com.ecommerce.auth.entity.Role;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.exception.UserAlreadyExistsException;
import com.ecommerce.auth.mapper.UserMapper;
import com.ecommerce.auth.repository.RoleRepository;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.security.JwtUtil;
import com.ecommerce.auth.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil jwtUtil;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = new Role(ERole.ROLE_USER);
        userRole.setId(1L);
    }

    @Test
    void register_withNewUsername_createsUserAndReturnsTokens() {
        RegisterRequest request = new RegisterRequest("vedha", "vedha@example.com", "StrongPass1", "Vedha", "J");

        when(userRepository.existsByUsername("vedha")).thenReturn(false);
        when(userRepository.existsByEmail("vedha@example.com")).thenReturn(false);
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("StrongPass1")).thenReturn("hashed-password");

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .username("vedha")
                .email("vedha@example.com")
                .password("hashed-password")
                .roles(Set.of(userRole))
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        when(jwtUtil.generateAccessToken(any(), anyString(), any())).thenReturn("access-token");
        when(jwtUtil.getAccessTokenExpirationMs()).thenReturn(900000L);

        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token("refresh-token-value")
                .user(savedUser)
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();
        when(refreshTokenService.createRefreshToken(savedUser)).thenReturn(refreshToken);

        AuthResponse response = authService.register(request);

        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token-value", response.refreshToken());
        assertEquals("Bearer", response.tokenType());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_withDuplicateUsername_throwsUserAlreadyExistsException() {
        RegisterRequest request = new RegisterRequest("vedha", "vedha@example.com", "StrongPass1", "Vedha", "J");
        when(userRepository.existsByUsername("vedha")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_withDuplicateEmail_throwsUserAlreadyExistsException() {
        RegisterRequest request = new RegisterRequest("newuser", "vedha@example.com", "StrongPass1", "V", "J");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("vedha@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_withValidCredentials_returnsTokens() {
        LoginRequest request = new LoginRequest("vedha", "StrongPass1");
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("vedha")
                .email("vedha@example.com")
                .roles(Set.of(userRole))
                .build();

        when(userRepository.findByUsernameOrEmail("vedha", "vedha")).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken(any(), anyString(), any())).thenReturn("access-token");
        when(jwtUtil.getAccessTokenExpirationMs()).thenReturn(900000L);

        RefreshToken refreshToken = RefreshToken.builder()
                .token("refresh-token-value")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();
        when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshToken);

        AuthResponse response = authService.login(request);

        assertEquals("access-token", response.accessToken());
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void refresh_withValidToken_rotatesAndReturnsNewAccessToken() {
        User user = User.builder().id(UUID.randomUUID()).username("vedha").roles(Set.of(userRole)).build();
        RefreshToken oldToken = RefreshToken.builder().token("old-token").user(user)
                .expiryDate(Instant.now().plusSeconds(3600)).build();
        RefreshToken newToken = RefreshToken.builder().token("new-token").user(user)
                .expiryDate(Instant.now().plusSeconds(3600)).build();

        when(refreshTokenService.verifyAndGet("old-token")).thenReturn(oldToken);
        when(refreshTokenService.rotate(oldToken)).thenReturn(newToken);
        when(jwtUtil.generateAccessToken(any(), anyString(), any())).thenReturn("new-access-token");
        when(jwtUtil.getAccessTokenExpirationMs()).thenReturn(900000L);

        AuthResponse response = authService.refresh("old-token");

        assertEquals("new-access-token", response.accessToken());
        assertEquals("new-token", response.refreshToken());
    }
}
