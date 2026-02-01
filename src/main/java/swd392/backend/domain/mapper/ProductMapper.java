package swd392.backend.domain.mapper;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;
import swd392.backend.domain.dto.ProductDTO;
import swd392.backend.jpa.model.Product;

@Component
@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductDTO toDto (Product product);
    Product toEntity (ProductDTO productDTO);
}
