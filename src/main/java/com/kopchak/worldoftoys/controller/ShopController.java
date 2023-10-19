package com.kopchak.worldoftoys.controller;

import com.kopchak.worldoftoys.dto.product.FilteredProductsPageDto;
import com.kopchak.worldoftoys.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<FilteredProductsPageDto> getAllProducts(@RequestParam(name = "page", defaultValue = "0") int page,
                                                                  @RequestParam(name = "size", defaultValue = "10") int size,
                                                                  @RequestParam(name = "name", required = false) String productName,
                                                                  @RequestParam(name = "min-price", required = false) BigDecimal minPrice,
                                                                  @RequestParam(name = "max-price", required = false) BigDecimal maxPrice,
                                                                  @RequestParam(name = "origin", required = false) List<String> originCategories,
                                                                  @RequestParam(name = "brand", required = false) List<String> brandCategories,
                                                                  @RequestParam(name = "age", required = false) List<String> ageCategories,
                                                                  @RequestParam(name = "price-sort", required = false) String priceSortOrder
    ) {
        var productsPage = productService.getAllProducts(page, size, productName, minPrice, maxPrice,
                originCategories, brandCategories, ageCategories, priceSortOrder);
        return new ResponseEntity<>(productsPage, HttpStatus.OK);
    }
}
