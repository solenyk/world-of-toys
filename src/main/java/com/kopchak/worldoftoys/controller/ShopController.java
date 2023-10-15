package com.kopchak.worldoftoys.controller;

import com.kopchak.worldoftoys.dto.product.ProductDto;
import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@CrossOrigin
@RequiredArgsConstructor
@Tag(name = "shop-controller", description = "")
public class ShopController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts(@RequestParam(name = "page", defaultValue = "0") int page,
                                                           @RequestParam(name = "size", defaultValue = "10") int size
    ){
        var productsPage = productService.getAllProducts(page, size);
        return new ResponseEntity<>(productsPage, HttpStatus.OK);
    }
}
