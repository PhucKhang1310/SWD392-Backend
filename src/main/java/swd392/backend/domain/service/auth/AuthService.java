package swd392.backend.domain.service.auth;

import swd392.backend.domain.dto.AuthResponseDTO;
import swd392.backend.domain.dto.LoginRequestDTO;
import swd392.backend.domain.dto.RegisterRequestDTO;

/**
 * Authentication service interface.
 * This simple implementation can be extended later with:
 * - Spring Security integration
 * - JWT token generation
 * - OAuth2 support
 * - Password encryption with BCrypt
 */
public interface AuthService {

    /**
     * Authenticate user with email and password
     */
    AuthResponseDTO login(LoginRequestDTO request);

    /**
     * Register a new user
     */
    AuthResponseDTO register(RegisterRequestDTO request);
}
