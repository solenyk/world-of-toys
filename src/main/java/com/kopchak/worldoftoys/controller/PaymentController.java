package com.kopchak.worldoftoys.controller;

import com.kopchak.worldoftoys.dto.error.ResponseStatusExceptionDto;
import com.kopchak.worldoftoys.dto.payment.StripeCredentialsDto;
import com.kopchak.worldoftoys.exception.AppStripeException;
import com.kopchak.worldoftoys.exception.InvalidOrderException;
import com.kopchak.worldoftoys.service.PaymentService;
import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
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
@Tag(name = "payment-controller", description = "The payment controller is responsible for managing payment-related data. " +
        "It provides endpoints for stripe checkout and stripe payment webhook handling.")
public class PaymentController {
    private final PaymentService paymentService;
    private static final String STRIPE_HEADER = "Stripe-Signature";

    @Operation(summary = "Stripe checkout")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302",
                    description = "Stripe custom checkout link has been successfully created",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(
                    responseCode = "400",
                    description = "The order does not exist or has already been paid",
                    content = @Content(schema = @Schema(implementation = ResponseStatusExceptionDto.class
                    )))
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/{orderId}")
    public ResponseEntity<Void> stripeCheckout(@Valid @RequestBody StripeCredentialsDto credentialsDto,
                                               @PathVariable(name = "orderId") String orderId) {
        if (paymentService.isNonExistentOrPaidOrder(orderId)) {
            log.error("The order with id: {} does not exist or has already been paid!", orderId);
            throw new InvalidOrderException(HttpStatus.BAD_REQUEST, "The order does not exist or has already been paid!");
        }
        try {
            String stripeCheckoutUserUrl = paymentService.stripeCheckout(credentialsDto, orderId);
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(stripeCheckoutUserUrl)).build();
        } catch (StripeException e) {
            log.error(e.getMessage());
            throw new AppStripeException(HttpStatus.valueOf(e.getStatusCode()), e.getMessage());
        }
    }

    @Operation(summary = "Handle payment webhook")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Payment has been successfully handled",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Unable to read the request body or a Stripe error occurred while handling the webhook",
                    content = @Content(schema = @Schema(implementation = ResponseStatusExceptionDto.class
                    )))
    })
    @PostMapping("/webhook")
    public ResponseEntity<Void> handlePaymentWebhook(HttpServletRequest request) {
        try {
            String sigHeader = request.getHeader(STRIPE_HEADER);
            String requestBody = IOUtils.toString(request.getReader());
            paymentService.handlePaymentWebhook(sigHeader, requestBody);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IOException | StripeException e) {
            log.error("Stripe error: {}", e.getMessage());
            throw new AppStripeException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
