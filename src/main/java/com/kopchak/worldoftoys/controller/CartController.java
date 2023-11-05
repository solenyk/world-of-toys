package com.kopchak.worldoftoys.controller;

import com.kopchak.worldoftoys.dto.cart.AddCartItemDto;
import com.kopchak.worldoftoys.dto.cart.CartDto;
import com.kopchak.worldoftoys.service.CartService;
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
@Tag(name = "cart-controller", description = "")
public class CartController {

    private final CartService cartService;

    @PostMapping(value = "/add-product")
    public ResponseEntity<?> addProductToCart(@Valid @RequestBody AddCartItemDto addCartItemDto, Principal principal) {
        cartService.addProductToCart(addCartItemDto, principal.getName());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
