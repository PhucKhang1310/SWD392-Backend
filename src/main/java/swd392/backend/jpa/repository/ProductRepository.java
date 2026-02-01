package swd392.backend.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swd392.backend.jpa.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByName(String name);
    Optional<Product> findById(long id);
}
