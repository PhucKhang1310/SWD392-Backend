package swd392.backend.domain.service.cart;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import swd392.backend.domain.dto.AddToCartRequestDTO;
import swd392.backend.domain.dto.CartDTO;
import swd392.backend.domain.mapper.CartMapper;
import swd392.backend.jpa.model.Cart;
import swd392.backend.jpa.model.CartItem;
import swd392.backend.jpa.model.Product;
import swd392.backend.jpa.model.User;
import swd392.backend.jpa.repository.CartItemRepository;
import swd392.backend.jpa.repository.CartRepository;
import swd392.backend.jpa.repository.ProductRepository;
import swd392.backend.jpa.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartServiceImpl implements CartService {
    CartRepository cartRepository;
    CartItemRepository cartItemRepository;
    ProductRepository productRepository;
    UserRepository userRepository;
    CartMapper cartMapper;

    @Override
    public CartDTO getCartByUserId(Integer userId) {
        Cart cart = cartRepository.findByBuyerIdAndStatus(userId, "ACTIVE")
                .orElse(null);

        if (cart == null) {
            return null;
        }

        CartDTO dto = cartMapper.toDto(cart);
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        dto.setCartItems(cartMapper.toCartItemDtoList(items));
        return dto;
    }

    @Override
    @Transactional
    public CartDTO addProductToCart(AddToCartRequestDTO request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock");
        }

        // Get or create active cart
        Cart cart = cartRepository.findByBuyerIdAndStatus(request.getUserId(), "ACTIVE")
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setBuyer(user);
                    newCart.setStatus("ACTIVE");
                    return cartRepository.save(newCart);
                });

        // Check if product already in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), request.getProductId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(request.getQuantity());
            newItem.setPriceAtTime(product.getPrice());
            cartItemRepository.save(newItem);
        }

        CartDTO dto = cartMapper.toDto(cart);
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        dto.setCartItems(cartMapper.toCartItemDtoList(items));
        return dto;
    }

    @Override
    public CartDTO updateCartItemQuantity(Integer cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }

        Cart cart = cartItem.getCart();
        CartDTO dto = cartMapper.toDto(cart);
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        dto.setCartItems(cartMapper.toCartItemDtoList(items));
        return dto;
    }

    @Override
    public void removeCartItem(Integer cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    @Override
    @Transactional
    public void clearCart(Integer userId) {
        Cart cart = cartRepository.findByBuyerIdAndStatus(userId, "ACTIVE")
                .orElseThrow(() -> new RuntimeException("No active cart found"));
        cartItemRepository.deleteByCartId(cart.getId());
    }
}
