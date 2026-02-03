package swd392.backend.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd392.backend.jpa.model.Order;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByBuyerId(Integer buyerId);
    List<Order> findByStatus(String status);
}
