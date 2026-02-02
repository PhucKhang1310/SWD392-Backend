package swd392.backend.domain.mapper;

import org.springframework.stereotype.Component;
import swd392.backend.domain.dto.CartDTO;
import swd392.backend.domain.dto.CartItemDTO;
import swd392.backend.jpa.model.Cart;
import swd392.backend.jpa.model.CartItem;

import java.util.ArrayList;
import java.util.List;

@Component
public class CartMapper {

    public CartDTO toDto(Cart cart) {
        if (cart == null) {
            return null;
        }

        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());
        dto.setStatus(cart.getStatus());

        if (cart.getBuyer() != null) {
            dto.setBuyerId(cart.getBuyer().getId());
        }

        return dto;
    }

    public Cart toEntity(CartDTO cartDTO) {
        if (cartDTO == null) {
            return null;
        }

        Cart cart = new Cart();
        cart.setId(cartDTO.getId());
        cart.setStatus(cartDTO.getStatus());

        return cart;
    }

    public CartItemDTO toCartItemDto(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }

        CartItemDTO dto = new CartItemDTO();
        dto.setId(cartItem.getId());
        dto.setQuantity(cartItem.getQuantity());
        dto.setPriceAtTime(cartItem.getPriceAtTime());

        if (cartItem.getCart() != null) {
            dto.setCartId(cartItem.getCart().getId());
        }

        if (cartItem.getProduct() != null) {
            dto.setProductId(cartItem.getProduct().getId());
            dto.setProductName(cartItem.getProduct().getName());
        }

        return dto;
    }

    public List<CartItemDTO> toCartItemDtoList(List<CartItem> cartItems) {
        if (cartItems == null) {
            return null;
        }

        List<CartItemDTO> list = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            list.add(toCartItemDto(cartItem));
        }

        return list;
    }
}
