package com.replay.order;

import com.replay.auth.User;
import com.replay.cart.Cart;
import com.replay.cart.CartItem;
import com.replay.cart.CartRepository;
import com.replay.cart.CartItemRepository;
import com.replay.common.ResourceNotFoundException;
import com.replay.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;

    private OrderService orderService;

    private User user;
    private Cart cart;
    private Product product1;
    private Product product2;
    private CartItem cartItem1;
    private CartItem cartItem2;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, cartRepository, cartItemRepository);

        user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        product1 = new Product();
        product1.setId(1L);
        product1.setName("Console NES");
        product1.setSlug("console-nes");
        product1.setPrice(new BigDecimal("49.99"));
        product1.setStockQuantity(10);

        product2 = new Product();
        product2.setId(2L);
        product2.setName("Jeu Mario");
        product2.setSlug("jeu-mario");
        product2.setPrice(new BigDecimal("29.99"));
        product2.setStockQuantity(5);

        cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);

        cartItem1 = new CartItem();
        cartItem1.setId(1L);
        cartItem1.setCart(cart);
        cartItem1.setProduct(product1);
        cartItem1.setQuantity(2);

        cartItem2 = new CartItem();
        cartItem2.setId(2L);
        cartItem2.setCart(cart);
        cartItem2.setProduct(product2);
        cartItem2.setQuantity(1);

        cart.setItems(List.of(cartItem1, cartItem2));
    }

    @Test
    void createOrderSuccess() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });

        Order result = orderService.createOrder(user, "15 rue du Gaming, 75001 Paris");

        assertNotNull(result);
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals("15 rue du Gaming, 75001 Paris", result.getShippingAddress());
        assertEquals(2, result.getItems().size());
        assertEquals(new BigDecimal("129.97"), result.getTotalAmount()); // 49.99*2 + 29.99*1

        for (var item : result.getItems()) {
            assertNotNull(item.getUnitPrice());
            assertEquals(item.getProduct().getPrice(), item.getUnitPrice());
        }

        verify(cartItemRepository).deleteByCartId(1L);
    }

    @Test
    void createOrderEmptyCart() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        cart.setItems(List.of());

        assertThrows(IllegalArgumentException.class, () ->
                orderService.createOrder(user, "15 rue du Gaming"));
    }

    @Test
    void createOrderNoCart() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                orderService.createOrder(user, "15 rue du Gaming"));
    }

    @Test
    void getOrdersByUser() {
        PageRequest pageable = PageRequest.of(0, 10);
        Order order1 = new Order();
        order1.setId(1L);
        order1.setUser(user);
        Order order2 = new Order();
        order2.setId(2L);
        order2.setUser(user);
        Page<Order> page = new PageImpl<>(List.of(order1, order2));

        when(orderRepository.findByUserIdOrderByOrderDateDesc(1L, pageable)).thenReturn(page);

        Page<Order> result = orderService.getOrdersByUser(user, pageable);

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void getOrderByIdSuccess() {
        Order order = new Order();
        order.setId(1L);
        order.setUser(user);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order result = orderService.getOrderById(1L, user);

        assertEquals(1L, result.getId());
    }

    @Test
    void getOrderByIdNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                orderService.getOrderById(999L, user));
    }

    @Test
    void getOrderByIdForbidden() {
        User otherUser = new User();
        otherUser.setId(2L);

        Order order = new Order();
        order.setId(1L);
        order.setUser(otherUser);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(com.replay.common.ForbiddenException.class, () ->
                orderService.getOrderById(1L, user));
    }
}
