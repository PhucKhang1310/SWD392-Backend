package swd392.backend.domain.service.product;

import swd392.backend.domain.dto.ProductDTO;
import swd392.backend.jpa.model.Product;

import java.util.List;

public interface ProductService {
    List<ProductDTO> getAllProducts();
    Product findProductById(Long id);
    Product createProduct(Product product);
    Product updateProduct(Long id, Product product);
    void deleteProduct(Long id);
}
