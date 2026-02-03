package swd392.backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class OrderDTO {
    Integer id;
    Integer buyerId;
    String buyerName;
    Instant orderDate;
    String status;
    BigDecimal totalAmount;
    List<OrderItemDTO> orderItems;
}
