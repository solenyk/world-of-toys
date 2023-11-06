package com.kopchak.worldoftoys.controller;

import com.kopchak.worldoftoys.dto.cart.RequestCartItemDto;
import com.kopchak.worldoftoys.dto.cart.UserCartDetailsDto;
import com.kopchak.worldoftoys.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/cart")
@CrossOrigin
@RequiredArgsConstructor
@Slf4j
@Tag(name = "cart-controller", description = "The cart controller is responsible for managing the user's cart. " +
        "It provides endpoints for adding, editing, and deleting cart items, as well as for retrieving all cart items.")
@SecurityRequirement(name = "Bearer Authentication")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Add the product to the cart")
    @ApiResponse(
            responseCode = "201",
            description = "Product was successfully added to the cart",
            content = @Content(schema = @Schema(hidden = true))
    )
    @PostMapping(value = "/add-product")
    public ResponseEntity<?> addProductToCart(@Valid @RequestBody RequestCartItemDto requestCartItemDto, Principal principal) {
        cartService.addProductToCart(requestCartItemDto, principal.getName());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "Retrieve the contents of the shopping cart for the user")
    @ApiResponse(
            responseCode = "200",
            description = "The contents of the shopping cart were successfully fetched",
            content = @Content(schema = @Schema(hidden = true))
    )
    @GetMapping
    public ResponseEntity<UserCartDetailsDto> getUserCartDetails(Principal principal) {
        return new ResponseEntity<>(cartService.getUserCartDetails(principal.getName()), HttpStatus.OK);
    }

    @Operation(summary = "Update cart item quantity")
    @ApiResponse(
            responseCode = "204",
            description = "Cart item quantity was successfully updated",
            content = @Content(schema = @Schema(hidden = true))
    )
    @PatchMapping
    public ResponseEntity<?> updateUserCartItem(@Valid @RequestBody RequestCartItemDto requestCartItemDto,
                                                Principal principal) {
        cartService.updateUserCartItem(requestCartItemDto, principal.getName());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Delete product from the cart")
    @ApiResponse(
            responseCode = "204",
            description = "Product was successfully deleted from the cart",
            content = @Content(schema = @Schema(hidden = true))
    )
    @DeleteMapping
    public ResponseEntity<?> deleteUserCartItem(@Valid @RequestBody RequestCartItemDto requestCartItemDto,
                                                Principal principal) {
        cartService.deleteUserCartItem(requestCartItemDto, principal.getName());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
