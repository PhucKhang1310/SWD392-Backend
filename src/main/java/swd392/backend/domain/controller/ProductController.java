package swd392.backend.domain.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import swd392.backend.domain.dto.ProductDTO;
import swd392.backend.domain.service.product.ProductService;

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

    @GetMapping("/{id}")
    public ProductDTO findById(@PathVariable Integer id) {
        return productService.findProductById(id);
    }

    @PostMapping
    public ProductDTO createProduct(@RequestBody ProductDTO productDTO) {
        return productService.createProduct(productDTO);
    }

    @PutMapping
    public ProductDTO updateProduct(@RequestBody ProductDTO productDTO) {
        return productService.updateProduct(productDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteProductById(@PathVariable Integer id) {
        productService.deleteProduct(id);
    }
}
