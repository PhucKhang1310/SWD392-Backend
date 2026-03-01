package swd392.backend.domain.service.product;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import swd392.backend.domain.dto.ProductDTO;
import swd392.backend.domain.mapper.ProductMapper;
import swd392.backend.domain.service.storage.StorageService;
import swd392.backend.jpa.model.Product;
import swd392.backend.jpa.repository.ProductRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements ProductService {
    ProductRepository productRepository;
    ProductMapper productMapper;
    StorageService storageService;

    @Override
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toDto)
                .map(this::convertImageUrlToPresigned)
                .toList();
    }

    @Override
    public ProductDTO findProductById(Integer id) {
        ProductDTO productDTO = productMapper.toDto(productRepository.findById(id).get());
        return convertImageUrlToPresigned(productDTO);
    }

    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = productMapper.toEntity(productDTO);
        Product savedProduct = productRepository.save(product);
        return convertImageUrlToPresigned(productMapper.toDto(savedProduct));
    }

    @Override
    public ProductDTO updateProduct(ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(productDTO.getId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        existingProduct.setName(productDTO.getName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setStockQuantity(productDTO.getStockQuantity());
        existingProduct.setStatus(productDTO.getStatus());
        existingProduct.setCategory(productDTO.getCategory());

        // Only update imgUrl if it's provided and is not a presigned URL
        if (productDTO.getImgUrl() != null && !productDTO.getImgUrl().isEmpty()) {
            existingProduct.setImgUrl(productDTO.getImgUrl());
        }

        Product updatedProduct = productRepository.save(existingProduct);
        return convertImageUrlToPresigned(productMapper.toDto(updatedProduct));
    }

    @Override
    public void deleteProduct(Integer id) {
        productRepository.deleteById(id);
    }

    /**
     * Convert MinIO object name to presigned URL for image access
     */
    private ProductDTO convertImageUrlToPresigned(ProductDTO productDTO) {
        if (productDTO.getImgUrl() != null && !productDTO.getImgUrl().isEmpty()) {
            // Only convert if it's not already a full URL
            if (!productDTO.getImgUrl().startsWith("http")) {
                String presignedUrl = storageService.getFileUrl(productDTO.getImgUrl(), "");
                productDTO.setImgUrl(presignedUrl);
            }
        }
        return productDTO;
    }
}
