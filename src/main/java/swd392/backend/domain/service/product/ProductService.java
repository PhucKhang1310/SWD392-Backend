package swd392.backend.domain.service.product;

import swd392.backend.domain.dto.ProductDTO;
import swd392.backend.jpa.model.Product;

import java.util.List;

public interface ProductService {
    List<ProductDTO> getAllProducts();
    ProductDTO findProductById(Integer id);
    ProductDTO createProduct(ProductDTO productDTO);
    ProductDTO updateProduct(ProductDTO productDTO);
    void deleteProduct(Integer id);
}
