package com.kopchak.worldoftoys.controller;

import com.kopchak.worldoftoys.dto.error.ExceptionDto;
import com.kopchak.worldoftoys.dto.payment.StripeCredentialsDto;
import com.kopchak.worldoftoys.service.impl.PaymentService;
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
@Tag(name = "payment-controller", description = "The payment controller is responsible for managing payment-related data. " +
        "It provides endpoints for stripe checkout and stripe payment webhook handling.")
public class PaymentController {
    private final PaymentService paymentService;
    private static final String STRIPE_HEADER = "Stripe-Signature";

    @Operation(summary = "Stripe checkout")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302",
                    description = "The stripe custom checkout link has been successfully created",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(
                    responseCode = "400",
                    description = "The order does not exist or has already been paid",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class
                    )))
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/{orderId}")
    public ResponseEntity<Void> stripeCheckout(@Valid @RequestBody StripeCredentialsDto credentialsDto,
                                               @PathVariable(name = "orderId") String orderId) throws StripeException {
        String stripeCheckoutUserUrl = paymentService.stripeCheckout(credentialsDto, orderId);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(stripeCheckoutUserUrl)).build();
    }

    @Operation(summary = "Handle payment webhook")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "The payment has been successfully handled",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Unable to read the request body",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class
                    ))),
            @ApiResponse(
                    responseCode = "503",
                    description = "Service Unavailable",
                    content = @Content(schema = @Schema(implementation = ExceptionDto.class
                    )))
    })
    @PostMapping("/webhook")
    public ResponseEntity<Void> handlePaymentWebhook(HttpServletRequest request) throws IOException, StripeException {
        String sigHeader = request.getHeader(STRIPE_HEADER);
        String requestBody = IOUtils.toString(request.getReader());
        paymentService.handlePaymentWebhook(sigHeader, requestBody);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
