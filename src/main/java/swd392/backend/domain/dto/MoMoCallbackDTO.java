package swd392.backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class MoMoCallbackDTO {
    String partnerCode;
    String orderId;
    String requestId;
    Long amount;
    String orderInfo;
    String orderType;
    Long transId;
    Integer resultCode;
    String message;
    String payType;
    Long responseTime;
    String extraData;
    String signature;
}
