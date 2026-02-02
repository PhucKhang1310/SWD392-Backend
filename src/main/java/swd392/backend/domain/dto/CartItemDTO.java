package swd392.backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class CartItemDTO {
    Integer id;
    Integer cartId;
    Integer productId;
    String productName;
    Integer quantity;
    BigDecimal priceAtTime;
}
