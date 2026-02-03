package swd392.backend.domain.service.cart;

import swd392.backend.domain.dto.AddToCartRequestDTO;
import swd392.backend.domain.dto.CartDTO;

public interface CartService {
    CartDTO getCartByUserId(Integer userId);
    CartDTO addProductToCart(AddToCartRequestDTO request);
    CartDTO updateCartItemQuantity(Integer cartItemId, Integer quantity);
    void removeCartItem(Integer cartItemId);
    void clearCart(Integer userId);
}
