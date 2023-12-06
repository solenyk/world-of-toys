package com.kopchak.worldoftoys.controller;

import com.kopchak.worldoftoys.dto.admin.product.AdminFilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.product.FilteredProductsPageDto;
import com.kopchak.worldoftoys.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin
@RequiredArgsConstructor
@Tag(name = "admin-panel-controller", description = "")
@Slf4j
public class AdminPanelController {

    private final ProductService productService;

    @Operation(summary = "Fetch filtered products")
    @ApiResponse(
            responseCode = "200",
            description = "Products were successfully fetched",
            content = @Content(schema = @Schema(implementation = FilteredProductsPageDto.class)))
    @GetMapping("/products")
    public ResponseEntity<AdminFilteredProductsPageDto> getAdminFilteredProducts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "name", required = false) String productName,
            @RequestParam(name = "min-price", required = false) BigDecimal minPrice,
            @RequestParam(name = "max-price", required = false) BigDecimal maxPrice,
            @RequestParam(name = "origin", required = false) List<String> originCategories,
            @RequestParam(name = "brand", required = false) List<String> brandCategories,
            @RequestParam(name = "age", required = false) List<String> ageCategories,
            @RequestParam(name = "price-sort", required = false) String priceSortOrder
    ) {
        var productsPage = productService.getAdminFilteredProducts(page, size, productName, minPrice, maxPrice,
                originCategories, brandCategories, ageCategories, priceSortOrder);
        return new ResponseEntity<>(productsPage, HttpStatus.OK);
    }
}
