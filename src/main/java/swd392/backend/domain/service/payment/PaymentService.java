package swd392.backend.domain.service.payment;

import swd392.backend.domain.dto.CheckoutResponseDTO;
import swd392.backend.domain.dto.MoMoCallbackDTO;

public interface PaymentService {
    CheckoutResponseDTO createCheckout(Integer orderId, String returnUrl, String notifyUrl) throws Exception;

    void handleMoMoCallback(MoMoCallbackDTO callback);
}
