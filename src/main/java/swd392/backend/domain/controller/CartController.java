package swd392.backend.domain.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swd392.backend.domain.dto.AddToCartRequestDTO;
import swd392.backend.domain.dto.CartDTO;
import swd392.backend.domain.service.cart.CartService;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping("/{userId}")
    public ResponseEntity<CartDTO> getCartByUserId(@PathVariable Integer userId) {
        CartDTO cart = cartService.getCartByUserId(userId);
        if (cart == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/add")
    public ResponseEntity<CartDTO> addProductToCart(@RequestBody AddToCartRequestDTO request) {
        try {
            CartDTO cart = cartService.addProductToCart(request);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/item/{cartItemId}")
    public ResponseEntity<CartDTO> updateCartItemQuantity(@PathVariable Integer cartItemId, @RequestParam Integer quantity) {
        try {
            CartDTO cart = cartService.updateCartItemQuantity(cartItemId, quantity);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<Void> removeCartItem(@PathVariable Integer cartItemId) {
        try {
            cartService.removeCartItem(cartItemId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<Void> clearCart(@PathVariable Integer userId) {
        try {
            cartService.clearCart(userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
