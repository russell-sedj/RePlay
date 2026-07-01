package com.replay.payment;

import com.replay.auth.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/pay")
    public ResponseEntity<PaymentResponse> pay(@AuthenticationPrincipal User user,
                                                @RequestBody PayRequest request) {
        PaymentResponse response = paymentService.pay(request.orderId(), request.method(), user);
        return ResponseEntity.ok(response);
    }

    public record PayRequest(
            Long orderId,
            String method
    ) {}
}
