package com.kopchak.worldoftoys.controller;

import com.kopchak.worldoftoys.dto.payment.StripeCredentialsDto;
import com.kopchak.worldoftoys.exception.InvalidOrderException;
import com.kopchak.worldoftoys.service.PaymentService;
import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/payment")
@CrossOrigin
@RequiredArgsConstructor
@Slf4j
@Tag(name = "payment-controller", description = "")
@SecurityRequirement(name = "Bearer Authentication")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/{orderId}")
    public ResponseEntity<Void> stripeCheckout(@Valid @RequestBody StripeCredentialsDto credentialsDto,
                                            @PathVariable(name = "orderId") String orderId) {
        if(paymentService.isNonExistentOrPaidOrder(orderId)){
            throw new InvalidOrderException(HttpStatus.BAD_REQUEST, "Order is paid or doesn't exist!");
        }
        String stripeCheckoutUserUrl = paymentService.stripeCheckout(credentialsDto, orderId);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(stripeCheckoutUserUrl)).build();
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> handlePaymentWebhook(HttpServletRequest request) throws StripeException, IOException {
        paymentService.handlePaymentWebhook(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
