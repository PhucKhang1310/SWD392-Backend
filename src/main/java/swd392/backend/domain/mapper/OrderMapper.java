package swd392.backend.domain.mapper;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.ObjectNotFoundException;
import org.springframework.stereotype.Component;
import swd392.backend.domain.dto.OrderDTO;
import swd392.backend.domain.dto.OrderItemDTO;
import swd392.backend.jpa.model.Order;
import swd392.backend.jpa.model.OrderItem;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class OrderMapper {

    public OrderDTO toDto(Order order) {
        if (order == null) {
            return null;
        }

        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());

        if (order.getBuyer() != null) {
            dto.setBuyerId(order.getBuyer().getId());
            dto.setBuyerName(order.getBuyer().getFullName());
        }

        return dto;
    }

    public Order toEntity(OrderDTO orderDTO) {
        if (orderDTO == null) {
            return null;
        }

        Order order = new Order();
        order.setId(orderDTO.getId());
        order.setOrderDate(orderDTO.getOrderDate());
        order.setStatus(orderDTO.getStatus());
        order.setTotalAmount(orderDTO.getTotalAmount());

        return order;
    }

    public OrderItemDTO toOrderItemDto(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }

        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(orderItem.getId());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPrice(orderItem.getPrice());

        if (orderItem.getOrder() != null) {
            dto.setOrderId(orderItem.getOrder().getId());
        }

        if (orderItem.getProduct() != null) {
            try {
                dto.setProductId(orderItem.getProduct().getId());
                dto.setProductName(orderItem.getProduct().getName());
            } catch (ObjectNotFoundException | EntityNotFoundException e) {
                log.warn("Product not found for order item {}: {}", orderItem.getId(), e.getMessage());
                dto.setProductId(null);
                dto.setProductName("[Product Deleted]");
            }
        }

        return dto;
    }

    public List<OrderItemDTO> toOrderItemDtoList(List<OrderItem> orderItems) {
        if (orderItems == null) {
            return null;
        }

        List<OrderItemDTO> list = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            list.add(toOrderItemDto(orderItem));
        }

        return list;
    }
}
