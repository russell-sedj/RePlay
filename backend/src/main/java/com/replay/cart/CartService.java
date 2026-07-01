package com.replay.cart;

import com.replay.auth.User;
import com.replay.common.ResourceNotFoundException;
import com.replay.product.Product;
import com.replay.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    @Transactional
    public CartItem addItem(User user, Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        Cart cart = getOrCreateCart(user);

        int existingQuantity = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .map(CartItem::getQuantity)
                .orElse(0);

        int newTotalQuantity = existingQuantity + quantity;
        if (newTotalQuantity > product.getStockQuantity()) {
            throw new IllegalArgumentException(
                    "Not enough stock available. Current stock: " + product.getStockQuantity());
        }

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setProduct(product);
                    return newItem;
                });

        item.setQuantity(newTotalQuantity);
        return cartItemRepository.save(item);
    }

    @Transactional
    public CartItem updateItemQuantity(User user, Long itemId, int quantity) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new com.replay.common.ForbiddenException("Access denied");
        }

        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        if (quantity > item.getProduct().getStockQuantity()) {
            throw new IllegalArgumentException(
                    "Not enough stock available. Current stock: " + item.getProduct().getStockQuantity());
        }

        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    @Transactional
    public void removeItem(User user, Long itemId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new com.replay.common.ForbiddenException("Access denied");
        }

        cartItemRepository.delete(item);
    }

    @Transactional
    public void clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        cartItemRepository.deleteByCartId(cart.getId());
    }

    public Cart getCart(User user) {
        return getOrCreateCart(user);
    }
}
