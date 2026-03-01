package swd392.backend.domain.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import swd392.backend.domain.dto.ProductDTO;
import swd392.backend.domain.service.product.ProductService;
import swd392.backend.domain.service.storage.StorageService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final StorageService storageService;

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

    @PostMapping("/{id}/upload-image")
    public ResponseEntity<Map<String, String>> uploadProductImage(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file) {
        try {
            // Upload file to MinIO
            String objectName = storageService.uploadFile(file, "products");

            // Get presigned URL
            String fileUrl = storageService.getFileUrl(objectName, "");

            // Update product with the objectName (stored in database)
            ProductDTO product = productService.findProductById(id);
            product.setImgUrl(objectName); // Store the object name, not the full URL
            productService.updateProduct(product);

            return ResponseEntity.ok(Map.of(
                    "message", "Image uploaded successfully",
                    "fileName", objectName,
                    "fileUrl", fileUrl));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to upload image: " + e.getMessage()));
        }
    }
}
