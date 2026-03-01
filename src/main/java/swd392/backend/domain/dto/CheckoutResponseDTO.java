package swd392.backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class CheckoutResponseDTO {
    Integer orderId;
    String payUrl;
    String deeplink;
    String qrCodeUrl;
    Integer resultCode;
    String message;
    String requestId;
    Long amount;
}
