package swd392.backend.domain.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swd392.backend.domain.dto.AuthResponseDTO;
import swd392.backend.domain.dto.LoginRequestDTO;
import swd392.backend.domain.dto.RegisterRequestDTO;
import swd392.backend.domain.service.auth.AuthService;

/**
 * Authentication controller for login and registration.
 *
 * Endpoints are publicly accessible. When adding Spring Security,
 * configure these endpoints to permitAll() in SecurityFilterChain.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Login endpoint
     * POST /api/auth/login
     *
     * @param request LoginRequestDTO with email and password
     * @return AuthResponseDTO with user info or error message
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO response = authService.login(request);

        if (response.getId() == null) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Register endpoint
     * POST /api/auth/register
     *
     * @param request RegisterRequestDTO with email, password, and fullName
     * @return AuthResponseDTO with new user info or error message
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        AuthResponseDTO response = authService.register(request);

        if (response.getId() == null) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }
}
