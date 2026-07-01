package com.replay.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserProfileDTO> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UserProfileDTO(user.getId(), user.getEmail(),
                        user.getFirstName(), user.getLastName(), user.getRole().name()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request.email(), request.password()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new UserProfileDTO(
                user.getId(), user.getEmail(),
                user.getFirstName(), user.getLastName(), user.getRole().name()
        ));
    }

    public record RegisterRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8) String password,
            @NotBlank String firstName,
            @NotBlank String lastName
    ) {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    public record RefreshRequest(
            @NotBlank String refreshToken
    ) {}
}
