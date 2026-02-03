package swd392.backend.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd392.backend.jpa.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByFullName(String fullName);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
