package com.replay.cart;

import com.replay.auth.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Gestion du panier")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartDTO> getCart(@AuthenticationPrincipal User user) {
        Cart cart = cartService.getCart(user);
        return ResponseEntity.ok(toCartDTO(cart));
    }

    @PostMapping("/items")
    public ResponseEntity<CartDTO> addItem(@AuthenticationPrincipal User user,
                                            @Valid @RequestBody AddItemRequest request) {
        cartService.addItem(user, request.productId(), request.quantity());
        Cart cart = cartService.getCart(user);
        return ResponseEntity.ok(toCartDTO(cart));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartDTO> updateItem(@AuthenticationPrincipal User user,
                                               @PathVariable Long itemId,
                                               @Valid @RequestBody UpdateItemRequest request) {
        cartService.updateItemQuantity(user, itemId, request.quantity());
        Cart cart = cartService.getCart(user);
        return ResponseEntity.ok(toCartDTO(cart));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDTO> removeItem(@AuthenticationPrincipal User user,
                                               @PathVariable Long itemId) {
        cartService.removeItem(user, itemId);
        Cart cart = cartService.getCart(user);
        return ResponseEntity.ok(toCartDTO(cart));
    }

    @DeleteMapping
    public ResponseEntity<CartDTO> clearCart(@AuthenticationPrincipal User user) {
        cartService.clearCart(user);
        Cart cart = cartService.getCart(user);
        return ResponseEntity.ok(toCartDTO(cart));
    }

    private CartDTO toCartDTO(Cart cart) {
        List<CartItemDTO> items = cart.getItems().stream()
                .map(item -> new CartItemDTO(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getSlug(),
                        item.getProduct().getImageUrl(),
                        item.getProduct().getPrice(),
                        item.getQuantity(),
                        item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                ))
                .toList();

        BigDecimal total = items.stream()
                .map(CartItemDTO::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartDTO(cart.getId(), items, total, items.isEmpty());
    }

    public record AddItemRequest(
            @NotNull Long productId,
            @Min(1) int quantity
    ) {}

    public record UpdateItemRequest(
            @Min(1) int quantity
    ) {}
}
