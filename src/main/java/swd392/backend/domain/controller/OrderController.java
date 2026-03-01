package swd392.backend.domain.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swd392.backend.domain.dto.*;
import swd392.backend.domain.service.order.OrderService;
import swd392.backend.domain.service.payment.PaymentService;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final PaymentService paymentService;

    @GetMapping
    public List<OrderDTO> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Integer orderId) {
        try {
            OrderDTO order = orderService.getOrderById(orderId);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}")
    public List<OrderDTO> getOrdersByUserId(@PathVariable Integer userId) {
        return orderService.getOrdersByUserId(userId);
    }

    @GetMapping("/{orderId}/items")
    public List<OrderItemDTO> getProductsInOrder(@PathVariable Integer orderId) {
        return orderService.getProductsInOrder(orderId);
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody CreateOrderRequestDTO request) {
        try {
            OrderDTO order = orderService.createOrderFromCart(request);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(@PathVariable Integer orderId, @RequestParam String status) {
        try {
            OrderDTO order = orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Integer orderId) {
        try {
            orderService.deleteOrder(orderId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{orderId}/checkout")
    public ResponseEntity<CheckoutResponseDTO> checkout(@PathVariable Integer orderId,
            @RequestBody CheckoutRequestDTO request) {
        try {
            CheckoutResponseDTO response = paymentService.createCheckout(orderId, request.getReturnUrl(),
                    request.getNotifyUrl());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/momo/callback")
    public ResponseEntity<String> momoCallback(@RequestBody MoMoCallbackDTO callback) {
        try {
            paymentService.handleMoMoCallback(callback);
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed: " + e.getMessage());
        }
    }
}
