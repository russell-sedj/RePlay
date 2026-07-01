package com.replay.order;

import com.replay.auth.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDetailDTO> createOrder(@AuthenticationPrincipal User user,
                                                       @Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(user, request.shippingAddress());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDetailDTO(order));
    }

    @GetMapping
    public ResponseEntity<Page<OrderSummaryDTO>> getOrders(@AuthenticationPrincipal User user,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size) {
        Page<Order> orders = orderService.getOrdersByUser(user, PageRequest.of(page, size));
        Page<OrderSummaryDTO> dtoPage = orders.map(this::toSummaryDTO);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailDTO> getOrder(@AuthenticationPrincipal User user,
                                                    @PathVariable Long id) {
        Order order = orderService.getOrderById(id, user);
        return ResponseEntity.ok(toDetailDTO(order));
    }

    private OrderSummaryDTO toSummaryDTO(Order order) {
        return new OrderSummaryDTO(
                order.getId(), order.getOrderDate(),
                order.getStatus().name(), order.getTotalAmount(),
                order.getTransactionId(),
                order.getItems() != null ? order.getItems().size() : 0
        );
    }

    private OrderDetailDTO toDetailDTO(Order order) {
        return new OrderDetailDTO(
                order.getId(), order.getOrderDate(),
                order.getStatus().name(), order.getTotalAmount(),
                order.getTransactionId(), order.getShippingAddress(),
                order.getItems().stream().map(item -> new OrderItemDTO(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getSlug(),
                        item.getProduct().getImageUrl(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                )).toList()
        );
    }

    public record CreateOrderRequest(
            @NotBlank String shippingAddress
    ) {}
}
