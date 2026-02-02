package swd392.backend.domain.service.order;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import swd392.backend.domain.dto.CreateOrderRequestDTO;
import swd392.backend.domain.dto.OrderDTO;
import swd392.backend.domain.dto.OrderItemDTO;
import swd392.backend.domain.mapper.OrderMapper;
import swd392.backend.jpa.model.*;
import swd392.backend.jpa.repository.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderServiceImpl implements OrderService {
    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    CartRepository cartRepository;
    CartItemRepository cartItemRepository;
    UserRepository userRepository;
    ProductRepository productRepository;
    OrderMapper orderMapper;

    @Override
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(order -> {
                    OrderDTO dto = orderMapper.toDto(order);
                    List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
                    dto.setOrderItems(orderMapper.toOrderItemDtoList(items));
                    return dto;
                })
                .toList();
    }

    @Override
    public OrderDTO getOrderById(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        OrderDTO dto = orderMapper.toDto(order);
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        dto.setOrderItems(orderMapper.toOrderItemDtoList(items));
        return dto;
    }

    @Override
    public List<OrderDTO> getOrdersByUserId(Integer userId) {
        return orderRepository.findByBuyerId(userId).stream()
                .map(order -> {
                    OrderDTO dto = orderMapper.toDto(order);
                    List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
                    dto.setOrderItems(orderMapper.toOrderItemDtoList(items));
                    return dto;
                })
                .toList();
    }

    @Override
    public List<OrderItemDTO> getProductsInOrder(Integer orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return orderMapper.toOrderItemDtoList(items);
    }

    @Override
    @Transactional
    public OrderDTO createOrderFromCart(CreateOrderRequestDTO request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByBuyerIdAndStatus(request.getUserId(), "ACTIVE")
                .orElseThrow(() -> new RuntimeException("No active cart found"));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Calculate total amount
        BigDecimal totalAmount = cartItems.stream()
                .map(item -> item.getPriceAtTime().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create order
        Order order = new Order();
        order.setBuyer(user);
        order.setOrderDate(Instant.now());
        order.setStatus("PENDING");
        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        // Create order items from cart items
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getPriceAtTime());
            orderItemRepository.save(orderItem);

            // Update product stock
            Product product = cartItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        // Clear cart
        cartItemRepository.deleteByCartId(cart.getId());
        cart.setStatus("COMPLETED");
        cartRepository.save(cart);

        OrderDTO dto = orderMapper.toDto(savedOrder);
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(savedOrder.getId());
        dto.setOrderItems(orderMapper.toOrderItemDtoList(orderItems));
        return dto;
    }

    @Override
    public OrderDTO updateOrderStatus(Integer orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);
        OrderDTO dto = orderMapper.toDto(savedOrder);
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        dto.setOrderItems(orderMapper.toOrderItemDtoList(items));
        return dto;
    }

    @Override
    @Transactional
    public void deleteOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Delete all order items first
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        orderItemRepository.deleteAll(orderItems);

        // Delete the order
        orderRepository.delete(order);
    }
}
