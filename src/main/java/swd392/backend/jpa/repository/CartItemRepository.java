package swd392.backend.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import swd392.backend.jpa.model.CartItem;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    List<CartItem> findByCartId(Integer cartId);
    Optional<CartItem> findByCartIdAndProductId(Integer cartId, Integer productId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = ?1")
    void deleteByCartId(Integer cartId);
}
