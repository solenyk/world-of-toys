package com.kopchak.worldoftoys.controller;

import com.kopchak.worldoftoys.dto.admin.product.AddUpdateProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminFilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminProductDto;
import com.kopchak.worldoftoys.dto.error.ResponseStatusExceptionDto;
import com.kopchak.worldoftoys.dto.product.FilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.product.ProductDto;
import com.kopchak.worldoftoys.exception.ProductNotFoundException;
import com.kopchak.worldoftoys.exception.exception.CategoryNotFoundException;
import com.kopchak.worldoftoys.exception.exception.ImageException;
import com.kopchak.worldoftoys.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin
@RequiredArgsConstructor
@Tag(name = "admin-panel-controller", description = "")
@SecurityRequirement(name = "Bearer Authentication")
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

    @Operation(summary = "Fetch product by id")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Product was successfully fetched",
                    content = @Content(schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product with this id is not found",
                    content = @Content(schema = @Schema(implementation = ResponseStatusExceptionDto.class)))
    })
    @GetMapping("/products/{productId}")
    public ResponseEntity<AdminProductDto> getAdminProductDtoById(@PathVariable(name = "productId") Integer productId) {
        Optional<AdminProductDto> productDtoOptional = productService.getAdminProductDtoById(productId);
        if (productDtoOptional.isEmpty()) {
            log.error("Product with id: '{}' is not found.", productId);
            throw new ProductNotFoundException(HttpStatus.NOT_FOUND, "Product doesn't exist");
        }
        return new ResponseEntity<>(productDtoOptional.get(), HttpStatus.OK);
    }

    @Operation(summary = "Update product with id")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Product was successfully updated",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid product update data",
                    content = @Content(schema = @Schema(implementation = ResponseStatusExceptionDto.class)))
    })
    @PutMapping("/products/{productId}")
    public ResponseEntity<Void> updateProduct(@PathVariable(name = "productId") Integer productId,
                                              @Valid @RequestPart("product") AddUpdateProductDto addUpdateProductDto,
                                              @RequestPart("image") MultipartFile mainImageFile,
                                              @RequestPart("images") List<MultipartFile> imageFilesList) {
        try {
            productService.updateProduct(productId, addUpdateProductDto, mainImageFile, imageFilesList);
        } catch (CategoryNotFoundException | ImageException e) {
            log.error("The error: {} occurred while updating the product with id: {}", e.getMessage(), productId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
