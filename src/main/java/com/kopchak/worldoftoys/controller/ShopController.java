package com.kopchak.worldoftoys.controller;

import com.kopchak.worldoftoys.dto.error.ResponseStatusExceptionDto;
import com.kopchak.worldoftoys.dto.product.FilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.product.ProductDto;
import com.kopchak.worldoftoys.dto.product.category.FilteringProductCategoriesDto;
import com.kopchak.worldoftoys.exception.ProductNotFoundException;
import com.kopchak.worldoftoys.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/products")
@CrossOrigin
@RequiredArgsConstructor
@Slf4j
@Tag(name = "shop-controller", description = "The shop controller is responsible for managing product-related data. " +
        "It provides endpoints for filtering products, fetching product categories and single products by slug")
public class ShopController {
    private final ProductService productService;

    @Operation(summary = "Fetch filtered products")
    @ApiResponse(
            responseCode = "200",
            description = "Products were successfully fetched",
            content = @Content(schema = @Schema(implementation = FilteredProductsPageDto.class)))
    @GetMapping
    public ResponseEntity<FilteredProductsPageDto> getFilteredProducts(
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
        var productsPage = productService.getFilteredProducts(page, size, productName, minPrice, maxPrice,
                originCategories, brandCategories, ageCategories, priceSortOrder);
        return new ResponseEntity<>(productsPage, HttpStatus.OK);
    }

    @Operation(summary = "Fetch filtering product categories")
    @ApiResponse(
            responseCode = "200",
            description = "Product categories were successfully fetched",
            content = @Content(schema = @Schema(implementation = FilteringProductCategoriesDto.class)))
    @GetMapping("/categories")
    public ResponseEntity<FilteringProductCategoriesDto> getFilteringProductCategories(
            @RequestParam(name = "name", required = false) String productName,
            @RequestParam(name = "min-price", required = false) BigDecimal minPrice,
            @RequestParam(name = "max-price", required = false) BigDecimal maxPrice,
            @RequestParam(name = "origin", required = false) List<String> originCategories,
            @RequestParam(name = "brand", required = false) List<String> brandCategories,
            @RequestParam(name = "age", required = false) List<String> ageCategories
    ) {
        FilteringProductCategoriesDto filteringProductCategoriesDto = productService.getFilteringProductCategories(
                productName, minPrice, maxPrice, originCategories, brandCategories, ageCategories);
        return new ResponseEntity<>(filteringProductCategoriesDto, HttpStatus.OK);
    }

    @Operation(summary = "Fetch product by slug")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Product was successfully fetched",
                    content = @Content(schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product with this slug is not found",
                    content = @Content(schema = @Schema(implementation = ResponseStatusExceptionDto.class)))
    })
    @GetMapping("/{productSlug}")
    public ResponseEntity<ProductDto> getProductBySlug(@PathVariable(name = "productSlug") String productSlug) {
        Optional<ProductDto> productDtoOptional = productService.getProductDtoBySlug(productSlug);
        if (productDtoOptional.isEmpty()) {
            log.error("Product with slug: '{}' is not found.", productSlug);
            throw new ProductNotFoundException(HttpStatus.NOT_FOUND, "Product doesn't exist");
        }
        return new ResponseEntity<>(productDtoOptional.get(), HttpStatus.OK);
    }
}
