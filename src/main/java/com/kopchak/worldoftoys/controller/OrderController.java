package com.kopchak.worldoftoys.controller;

import com.kopchak.worldoftoys.dto.order.OrderDto;
import com.kopchak.worldoftoys.dto.order.OrderRecipientDto;
import com.kopchak.worldoftoys.model.user.AppUser;
import com.kopchak.worldoftoys.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "Create order")
    @ApiResponse(
            responseCode = "200",
            description = "Order has been successfully created",
            content = @Content(schema = @Schema(hidden = true)))
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
