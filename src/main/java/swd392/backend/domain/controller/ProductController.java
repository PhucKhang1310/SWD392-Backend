package swd392.backend.domain.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import swd392.backend.domain.dto.ProductDTO;
import swd392.backend.domain.service.product.ProductService;
import swd392.backend.domain.service.storage.StorageService;

import java.math.BigDecimal;
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

    @GetMapping("/{id}/image-url")
    public ResponseEntity<Map<String, String>> getProductImageUrl(@PathVariable Integer id) {
        try {
            ProductDTO product = productService.findProductById(id);

            if (product.getImgUrl() != null && !product.getImgUrl().isEmpty()) {
                // Generate presigned URL from object name
                String presignedUrl = storageService.getFileUrl(product.getImgUrl(), "");
                return ResponseEntity.ok(Map.of(
                        "objectName", product.getImgUrl(),
                        "presignedUrl", presignedUrl));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to get image URL: " + e.getMessage()));
        }
    }

    @PostMapping
    public ProductDTO createProduct(@RequestBody ProductDTO productDTO) {
        return productService.createProduct(productDTO);
    }

    @PostMapping(value = "/with-image", consumes = "multipart/form-data")
    public ResponseEntity<ProductDTO> createProductWithImage(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam("stockQuantity") Integer stockQuantity,
            @RequestParam("status") String status,
            @RequestParam("category") String category,
            @RequestParam("image") MultipartFile file) {
        try {
            // Upload image to MinIO
            String objectName = storageService.uploadFile(file, "products");

            // Create product DTO with image reference
            ProductDTO productDTO = new ProductDTO();
            productDTO.setName(name);
            productDTO.setDescription(description);
            productDTO.setPrice(price);
            productDTO.setStockQuantity(stockQuantity);
            productDTO.setStatus(status);
            productDTO.setCategory(category);
            productDTO.setImgUrl(objectName);

            // Create product with image reference
            ProductDTO createdProduct = productService.createProduct(productDTO);

            return ResponseEntity.ok(createdProduct);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping
    public ProductDTO updateProduct(@RequestBody ProductDTO productDTO) {
        return productService.updateProduct(productDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProductById(@PathVariable Integer id) {
        try {
            // Get product to find image
            ProductDTO product = productService.findProductById(id);

            // Delete image from MinIO if exists
            if (product.getImgUrl() != null && !product.getImgUrl().isEmpty()
                    && !product.getImgUrl().startsWith("http")) {
                try {
                    storageService.deleteFile(product.getImgUrl(), "");
                } catch (Exception e) {
                    // Log but continue deleting product even if image deletion fails
                    System.err.println("Failed to delete image: " + e.getMessage());
                }
            }

            // Delete product
            productService.deleteProduct(id);
            return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to delete product: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/upload-image")
    public ResponseEntity<Map<String, String>> uploadProductImage(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file) {
        try {
            // Get existing product
            ProductDTO product = productService.findProductById(id);

            // Delete old image if exists
            if (product.getImgUrl() != null && !product.getImgUrl().isEmpty()
                    && !product.getImgUrl().startsWith("http")) {
                try {
                    storageService.deleteFile(product.getImgUrl(), "");
                } catch (Exception e) {
                    System.err.println("Failed to delete old image: " + e.getMessage());
                }
            }

            // Upload new file to MinIO
            String objectName = storageService.uploadFile(file, "products");

            // Get presigned URL
            String fileUrl = storageService.getFileUrl(objectName, "");

            // Update product with the objectName (stored in database)
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

    @DeleteMapping("/{id}/delete-image")
    public ResponseEntity<Map<String, String>> deleteProductImage(@PathVariable Integer id) {
        try {
            ProductDTO product = productService.findProductById(id);

            if (product.getImgUrl() != null && !product.getImgUrl().isEmpty()
                    && !product.getImgUrl().startsWith("http")) {
                // Delete from MinIO
                storageService.deleteFile(product.getImgUrl(), "");

                // Clear from database
                product.setImgUrl(null);
                productService.updateProduct(product);

                return ResponseEntity.ok(Map.of("message", "Product image deleted successfully"));
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Product has no image to delete"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to delete image: " + e.getMessage()));
        }
    }
}
