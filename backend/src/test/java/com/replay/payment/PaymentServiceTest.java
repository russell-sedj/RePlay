package com.replay.payment;

import com.replay.auth.User;
import com.replay.common.ForbiddenException;
import com.replay.common.ResourceNotFoundException;
import com.replay.order.Order;
import com.replay.order.OrderItem;
import com.replay.order.OrderRepository;
import com.replay.order.OrderStatus;
import com.replay.product.Product;
import com.replay.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;

    private PaymentService paymentService;

    private User user;
    private Order order;
    private Product product1;
    private Product product2;
    private OrderItem orderItem1;
    private OrderItem orderItem2;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(orderRepository, productRepository);

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

        orderItem1 = new OrderItem();
        orderItem1.setProduct(product1);
        orderItem1.setQuantity(2);
        orderItem1.setUnitPrice(new BigDecimal("49.99"));

        orderItem2 = new OrderItem();
        orderItem2.setProduct(product2);
        orderItem2.setQuantity(1);
        orderItem2.setUnitPrice(new BigDecimal("29.99"));

        order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setItems(List.of(orderItem1, orderItem2));
        order.setTotalAmount(new BigDecimal("129.97"));
    }

    @Test
    void paySuccess() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(productRepository.save(product1)).thenReturn(product1);
        when(productRepository.save(product2)).thenReturn(product2);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse response = paymentService.pay(1L, "CARTE", user);

        assertTrue(response.success());
        assertNotNull(response.transactionId());
        assertTrue(response.transactionId().startsWith("TXN-"));
        assertEquals(1L, response.orderId());

        assertEquals(8, product1.getStockQuantity()); // 10 - 2
        assertEquals(4, product2.getStockQuantity()); // 5 - 1
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertNotNull(order.getTransactionId());
    }

    @Test
    void payOrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                paymentService.pay(999L, "CARTE", user));
    }

    @Test
    void payForbidden() {
        User otherUser = new User();
        otherUser.setId(2L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(ForbiddenException.class, () ->
                paymentService.pay(1L, "CARTE", otherUser));
    }

    @Test
    void payAlreadyConfirmed() {
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTransactionId("TXN-already");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () ->
                paymentService.pay(1L, "CARTE", user));
    }

    @Test
    void payInsufficientStock() {
        product1.setStockQuantity(1);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () ->
                paymentService.pay(1L, "CARTE", user));
    }

    @Test
    void payCancelledOrder() {
        order.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () ->
                paymentService.pay(1L, "CARTE", user));
    }
}
