package swd392.backend.domain.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import swd392.backend.domain.dto.ProductDTO;
import swd392.backend.domain.service.product.ProductService;
import swd392.backend.jpa.model.Product;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public List<ProductDTO> findAll() {
        return productService.getAllProducts();
    }

    @DeleteMapping
    public void deleteProductById(long id) {
        productService.deleteProduct(id);
    }
}
