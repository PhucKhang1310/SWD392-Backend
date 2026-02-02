package swd392.backend.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd392.backend.jpa.model.Cart;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    Optional<Cart> findByBuyerIdAndStatus(Integer buyerId, String status);
    Optional<Cart> findByBuyerId(Integer buyerId);
}
