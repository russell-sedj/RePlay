package com.replay.auth;

import com.replay.config.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public User register(String email, String password, String firstName, String lastName) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(Role.USER);

        return userRepository.save(user);
    }

    public AuthResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return new AuthResponse(accessToken, refreshToken, toUserProfile(user));
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        if (!jwtService.isRefreshTokenValid(refreshToken)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        String email = jwtService.extractEmailFromRefreshToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        String newAccessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole().name());
        String newRefreshToken = jwtService.generateRefreshToken(user.getEmail());

        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        return new AuthResponse(newAccessToken, newRefreshToken, toUserProfile(user));
    }

    public UserProfileDTO getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return toUserProfile(user);
    }

    private UserProfileDTO toUserProfile(User user) {
        return new UserProfileDTO(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name()
        );
    }
}
