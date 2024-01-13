package com.kopchak.worldoftoys.controller;

import com.kopchak.worldoftoys.dto.cart.RequestCartItemDto;
import com.kopchak.worldoftoys.dto.cart.UserCartDetailsDto;
import com.kopchak.worldoftoys.exception.ProductNotFoundException;
import com.kopchak.worldoftoys.domain.user.AppUser;
import com.kopchak.worldoftoys.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
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

@RestController
@RequestMapping("/api/v1/cart")
@CrossOrigin
@RequiredArgsConstructor
@Tag(name = "cart-controller", description = "The cart controller is responsible for managing the user's cart. " +
        "It provides endpoints for adding, editing, and deleting cart items, as well as for retrieving all cart items.")
@SecurityRequirement(name = "Bearer Authentication")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Add the product to the cart")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "The product was successfully added to the cart",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The product with this slug is not found",
                    content = @Content(schema = @Schema(implementation = ResponseStatusException.class))
            )
    })
    @PostMapping(value = "/add-product")
    public ResponseEntity<Void> addProductToCart(@Valid @RequestBody RequestCartItemDto requestCartItemDto,
                                                 @AuthenticationPrincipal AppUser user) {
        try {
            cartService.addProductToCart(requestCartItemDto, user);
        } catch (ProductNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "Retrieve the contents of the shopping cart for the user")
    @ApiResponse(
            responseCode = "200",
            description = "Contents of the shopping cart were successfully fetched",
            content = @Content(schema = @Schema(hidden = true))
    )
    @GetMapping
    public ResponseEntity<UserCartDetailsDto> getUserCartDetails(@AuthenticationPrincipal AppUser user) {
        return new ResponseEntity<>(cartService.getUserCartDetails(user), HttpStatus.OK);
    }

    @Operation(summary = "Update cart item quantity")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "The cart item quantity was successfully updated",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The product with this slug is not found",
                    content = @Content(schema = @Schema(implementation = ResponseStatusException.class))
            )
    })
    @PatchMapping
    public ResponseEntity<Void> updateUserCartItem(@Valid @RequestBody RequestCartItemDto requestCartItemDto,
                                                   @AuthenticationPrincipal AppUser user) {
        try {
            cartService.updateUserCartItem(requestCartItemDto, user);
        } catch (ProductNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Delete product from the cart")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "The product was successfully deleted from the cart",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The product with this slug is not found",
                    content = @Content(schema = @Schema(implementation = ResponseStatusException.class))
            )
    })
    @DeleteMapping
    public ResponseEntity<Void> deleteUserCartItem(@Valid @RequestBody RequestCartItemDto requestCartItemDto,
                                                   @AuthenticationPrincipal AppUser user) {
        try {
            cartService.deleteUserCartItem(requestCartItemDto, user);
        } catch (ProductNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
