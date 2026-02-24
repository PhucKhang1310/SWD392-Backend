package swd392.backend.domain.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swd392.backend.domain.dto.AuthResponseDTO;
import swd392.backend.domain.dto.LoginRequestDTO;
import swd392.backend.domain.dto.RegisterRequestDTO;
import swd392.backend.jpa.model.User;
import swd392.backend.jpa.repository.UserRepository;

/**
 * Simple authentication service implementation.
 *
 * TODO: For production, enhance with:
 * - PasswordEncoder (BCryptPasswordEncoder) for password hashing
 * - Spring Security for authentication management
 * - JWT or session-based token generation
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    // TODO: Inject PasswordEncoder when adding Spring Security
    // private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null) {
            return AuthResponseDTO.error("User not found");
        }

        // Simple password check (TODO: Use passwordEncoder.matches() with Spring Security)
        if (!user.getPassword().equals(request.getPassword())) {
            return AuthResponseDTO.error("Invalid password");
        }

        // Check if user is active
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            return AuthResponseDTO.error("Account is not active");
        }

        return AuthResponseDTO.success(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getRole()
        );
    }

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return AuthResponseDTO.error("Email is already registered");
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        // TODO: Use passwordEncoder.encode() when adding Spring Security
        user.setPassword(request.getPassword());
        user.setFullName(request.getFullName());
        user.setRole(request.getRole() != null ? request.getRole() : "USER");
        user.setStatus("ACTIVE");

        User savedUser = userRepository.save(user);

        return AuthResponseDTO.success(
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getFullName(),
            savedUser.getRole()
        );
    }
}
