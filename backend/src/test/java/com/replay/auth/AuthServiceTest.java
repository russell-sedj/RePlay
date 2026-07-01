package com.replay.auth;

import com.replay.common.DuplicateResourceException;
import com.replay.common.UnauthorizedException;
import com.replay.config.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void registerSuccess() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            u.setCreatedAt(java.time.LocalDateTime.now());
            return u;
        });

        User result = authService.register("test@test.com", "password123", "John", "Doe");

        assertNotNull(result);
        assertEquals("test@test.com", result.getEmail());
        assertTrue(passwordEncoder.matches("password123", result.getPassword()));
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals(Role.USER, result.getRole());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void registerDuplicateEmail() {
        when(userRepository.findByEmail("existing@test.com")).thenReturn(Optional.of(new User()));

        assertThrows(DuplicateResourceException.class, () ->
                authService.register("existing@test.com", "password123", "John", "Doe")
        );
    }

    @Test
    void loginSuccess() {
        String encodedPassword = passwordEncoder.encode("password123");
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setPassword(encodedPassword);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(Role.USER);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken("test@test.com", "USER")).thenReturn("access-token");
        when(jwtService.generateRefreshToken("test@test.com")).thenReturn("refresh-token");

        AuthResponse response = authService.login("test@test.com", "password123");

        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        assertEquals("test@test.com", response.user().email());
        assertNotNull(user.getRefreshToken());
    }

    @Test
    void loginInvalidPassword() {
        String encodedPassword = passwordEncoder.encode("correctpassword");
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword(encodedPassword);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        assertThrows(UnauthorizedException.class, () ->
                authService.login("test@test.com", "wrongpassword")
        );
    }

    @Test
    void loginNonExistentEmail() {
        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () ->
                authService.login("nonexistent@test.com", "password123")
        );
    }

    @Test
    void refreshWithValidToken() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setPassword("encoded");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(Role.USER);
        user.setRefreshToken("valid-refresh-token");

        when(jwtService.isRefreshTokenValid("valid-refresh-token")).thenReturn(true);
        when(jwtService.extractEmailFromRefreshToken("valid-refresh-token")).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken("test@test.com", "USER")).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken("test@test.com")).thenReturn("new-refresh-token");

        AuthResponse response = authService.refresh("valid-refresh-token");

        assertNotNull(response);
        assertEquals("new-access-token", response.accessToken());
        assertEquals("new-refresh-token", response.refreshToken());
    }

    @Test
    void refreshWithInvalidToken() {
        when(jwtService.isRefreshTokenValid("invalid-token")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () ->
                authService.refresh("invalid-token")
        );
    }

    @Test
    void refreshWithStaleToken() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setRefreshToken("different-refresh-token");

        when(jwtService.isRefreshTokenValid("stale-refresh-token")).thenReturn(true);
        when(jwtService.extractEmailFromRefreshToken("stale-refresh-token")).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        assertThrows(UnauthorizedException.class, () ->
                authService.refresh("stale-refresh-token")
        );
    }
}
