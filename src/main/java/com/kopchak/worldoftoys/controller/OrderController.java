package com.kopchak.worldoftoys.controller;

import com.kopchak.worldoftoys.dto.order.OrderDto;
import com.kopchak.worldoftoys.dto.order.OrderRecipientDto;
import com.kopchak.worldoftoys.domain.user.AppUser;
import com.kopchak.worldoftoys.service.impl.CartService;
import com.kopchak.worldoftoys.service.impl.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/order")
@CrossOrigin
@RequiredArgsConstructor
@Tag(name = "order-controller", description = "The order controller is responsible for managing order-related data. " +
        "It provides endpoints for creating orders and fetching all user's orders.")
@SecurityRequirement(name = "Bearer Authentication")
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;

    @GetMapping("/verify-cart")
    public ResponseEntity<Void> verifyCartBeforeOrderCreation(@AuthenticationPrincipal AppUser user) {
        cartService.verifyCartBeforeOrderCreation(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Create order")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "The order has been successfully created",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(
                    responseCode = "400",
                    description = "It is impossible to create an order for the user because there " +
                            "are no products in the user's cart.",
                    content = @Content(schema = @Schema(implementation = ResponseStatusException.class)))
    })
    @PostMapping
    public ResponseEntity<Void> createOrder(@Valid @RequestBody OrderRecipientDto orderRecipientDto,
                                            @AuthenticationPrincipal AppUser user) {
        orderService.createOrder(orderRecipientDto, user);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "Retrieve all user orders")
    @ApiResponse(
            responseCode = "200",
            description = "Orders for user have been successfully retrieved",
            content = {
                    @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = OrderDto.class))
                    )
            })
    @GetMapping
    public ResponseEntity<Set<OrderDto>> getAllUserOrders(@AuthenticationPrincipal AppUser user) {
        return new ResponseEntity<>(orderService.getAllUserOrders(user), HttpStatus.OK);
    }
}
