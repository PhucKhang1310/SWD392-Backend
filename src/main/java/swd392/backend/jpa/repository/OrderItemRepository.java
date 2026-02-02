package swd392.backend.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd392.backend.jpa.model.OrderItem;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    List<OrderItem> findByOrderId(Integer orderId);
    List<OrderItem> findByProductId(Integer productId);
}
