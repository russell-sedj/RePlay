package com.replay.payment;

import com.replay.auth.User;
import com.replay.common.ForbiddenException;
import com.replay.common.ResourceNotFoundException;
import com.replay.order.Order;
import com.replay.order.OrderRepository;
import com.replay.order.OrderStatus;
import com.replay.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional
    public PaymentResponse pay(Long orderId, String method, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Access denied");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Order cannot be paid. Current status: " + order.getStatus());
        }

        for (var item : order.getItems()) {
            var product = item.getProduct();
            if (product.getStockQuantity() < item.getQuantity()) {
                String msg = String.format(
                        "Insufficient stock for product: %s. Available: %d, Requested: %d",
                        product.getName(), product.getStockQuantity(), item.getQuantity());
                throw new IllegalArgumentException(msg);
            }
        }

        for (var item : order.getItems()) {
            var product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);
        }

        String transactionId = "TXN-" + UUID.randomUUID().toString();
        order.setTransactionId(transactionId);
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        return new PaymentResponse(true, transactionId, orderId);
    }
}
