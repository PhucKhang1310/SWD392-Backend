package swd392.backend.domain.service.order;

import swd392.backend.domain.dto.CreateOrderRequestDTO;
import swd392.backend.domain.dto.OrderDTO;
import swd392.backend.domain.dto.OrderItemDTO;

import java.util.List;

public interface OrderService {
    List<OrderDTO> getAllOrders();
    OrderDTO getOrderById(Integer orderId);
    List<OrderDTO> getOrdersByUserId(Integer userId);
    List<OrderItemDTO> getProductsInOrder(Integer orderId);
    OrderDTO createOrderFromCart(CreateOrderRequestDTO request);
    OrderDTO updateOrderStatus(Integer orderId, String status);
    void deleteOrder(Integer orderId);
}
