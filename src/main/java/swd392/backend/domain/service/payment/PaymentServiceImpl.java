package swd392.backend.domain.service.payment;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import swd392.backend.domain.dto.CheckoutResponseDTO;
import swd392.backend.domain.dto.MoMoCallbackDTO;
import swd392.backend.domain.service.order.OrderService;
import swd392.backend.jpa.model.Order;
import swd392.backend.jpa.repository.OrderRepository;
import swd392.backend.momo.config.Environment;
import swd392.backend.momo.enums.RequestType;
import swd392.backend.momo.models.PaymentResponse;
import swd392.backend.momo.processor.CreateOrderMoMo;

@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentServiceImpl implements PaymentService {
    OrderRepository orderRepository;
    OrderService orderService;

    @Override
    public CheckoutResponseDTO createCheckout(Integer orderId, String returnUrl, String notifyUrl) throws Exception {
        // Get order from database
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Initialize MoMo environment (using dev for now)
        Environment environment = Environment.selectEnv("dev");

        // Generate unique IDs
        String momoOrderId = "ORDER_" + orderId + "_" + System.currentTimeMillis();
        String requestId = "REQ_" + System.currentTimeMillis();
        String amount = order.getTotalAmount().multiply(java.math.BigDecimal.valueOf(100)).toBigInteger().toString();
        String orderInfo = "Payment for Order #" + orderId;
        String extraData = ""; // Can be used to store additional information

        // Create MoMo payment
        PaymentResponse momoResponse = CreateOrderMoMo.process(
                environment,
                momoOrderId,
                requestId,
                amount,
                orderInfo,
                returnUrl,
                notifyUrl,
                extraData,
                RequestType.CAPTURE_WALLET,
                Boolean.TRUE);

        // Build response
        CheckoutResponseDTO response = new CheckoutResponseDTO();
        if (momoResponse != null) {
            response.setOrderId(orderId);
            response.setPayUrl(momoResponse.getPayUrl());
            response.setDeeplink(momoResponse.getDeeplink());
            response.setQrCodeUrl(momoResponse.getQrCodeUrl());
            response.setResultCode(momoResponse.getResultCode());
            response.setMessage(momoResponse.getMessage());
            response.setRequestId(momoResponse.getRequestId());
            response.setAmount(momoResponse.getAmount());

            // Keep status as PENDING - order is waiting for payment confirmation
            // (No need to update status here as it's already PENDING)
        } else {
            throw new RuntimeException("Failed to create MoMo payment");
        }

        return response;
    }

    @Override
    public void handleMoMoCallback(MoMoCallbackDTO callback) {
        // Extract order ID from MoMo order ID (format: ORDER_{orderId}_{timestamp})
        String momoOrderId = callback.getOrderId();
        String[] parts = momoOrderId.split("_");

        if (parts.length < 2) {
            throw new RuntimeException("Invalid order ID format: " + momoOrderId);
        }

        Integer orderId = Integer.parseInt(parts[1]);

        // Update order status based on result code
        // resultCode = 0 means success
        if (callback.getResultCode() == 0) {
            orderService.updateOrderStatus(orderId, "CONFIRMED");
        } else {
            orderService.updateOrderStatus(orderId, "CANCELLED");
        }
    }
}
