package com.kopchak.worldoftoys.controller;

import com.kopchak.worldoftoys.dto.cart.RequestCartItemDto;
import com.kopchak.worldoftoys.dto.order.OrderRecipientDto;
import com.kopchak.worldoftoys.model.user.AppUser;
import com.kopchak.worldoftoys.service.OrderPaymentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/order")
@CrossOrigin
@RequiredArgsConstructor
@Slf4j
@Tag(name = "order-payment-controller", description = "")
//@SecurityRequirement(name = "Bearer Authentication")
public class OrderPaymentController {

    private final OrderPaymentService orderPaymentService;

    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRecipientDto orderRecipientDto,
                                              @AuthenticationPrincipal AppUser user) {
        System.out.println(orderRecipientDto.firstname());
        orderPaymentService.createOrder(orderRecipientDto, user);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
