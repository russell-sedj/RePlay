package com.replay.cart;

import com.replay.auth.User;
import com.replay.common.ResourceNotFoundException;
import com.replay.product.Product;
import com.replay.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ProductRepository productRepository;

    private CartService cartService;
    private User user;
    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        cartService = new CartService(cartRepository, cartItemRepository, productRepository);

        user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("29.99"));
        product.setStockQuantity(10);

        cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
    }

    @Test
    void getOrCreateCartExisting() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        Cart result = cartService.getOrCreateCart(user);

        assertEquals(cart.getId(), result.getId());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void getOrCreateCartNew() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any())).thenReturn(cart);

        Cart result = cartService.getOrCreateCart(user);

        assertNotNull(result);
        verify(cartRepository).save(any());
    }

    @Test
    void addItemCreatesNewItem() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(1L, 1L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CartItem result = cartService.addItem(user, 1L, 2);

        assertEquals(2, result.getQuantity());
        assertEquals(product, result.getProduct());
    }

    @Test
    void addItemIncrementsExisting() {
        CartItem existing = new CartItem();
        existing.setCart(cart);
        existing.setProduct(product);
        existing.setQuantity(1);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(1L, 1L)).thenReturn(Optional.of(existing));
        when(cartItemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CartItem result = cartService.addItem(user, 1L, 3);

        assertEquals(4, result.getQuantity());
    }

    @Test
    void addItemStockInsufficient() {
        product.setStockQuantity(5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                cartService.addItem(user, 1L, 10)
        );
    }

    @Test
    void removeItemSuccess() {
        CartItem item = new CartItem();
        item.setId(1L);
        item.setCart(cart);
        item.setProduct(product);

        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(item));

        cartService.removeItem(user, 1L);

        verify(cartItemRepository).delete(item);
    }

    @Test
    void removeItemWrongUser() {
        User otherUser = new User();
        otherUser.setId(2L);

        Cart otherCart = new Cart();
        otherCart.setUser(otherUser);

        CartItem item = new CartItem();
        item.setId(1L);
        item.setCart(otherCart);

        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(com.replay.common.ForbiddenException.class, () ->
                cartService.removeItem(user, 1L)
        );
    }

    @Test
    void clearCartSuccess() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        cartService.clearCart(user);

        verify(cartItemRepository).deleteByCartId(cart.getId());
    }
}
