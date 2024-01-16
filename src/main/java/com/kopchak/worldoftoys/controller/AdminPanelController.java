package com.kopchak.worldoftoys.controller;

import com.kopchak.worldoftoys.dto.admin.product.AddUpdateProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminFilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminProductDto;
import com.kopchak.worldoftoys.dto.admin.product.category.AdminCategoryDto;
import com.kopchak.worldoftoys.dto.admin.product.category.CategoryNameDto;
import com.kopchak.worldoftoys.dto.admin.product.order.FilteredOrdersPageDto;
import com.kopchak.worldoftoys.dto.admin.product.order.FilteringOrderOptionsDto;
import com.kopchak.worldoftoys.dto.admin.product.order.StatusDto;
import com.kopchak.worldoftoys.dto.error.ResponseStatusExceptionDto;
import com.kopchak.worldoftoys.dto.product.FilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.product.ProductDto;
import com.kopchak.worldoftoys.exception.*;
import com.kopchak.worldoftoys.domain.order.OrderStatus;
import com.kopchak.worldoftoys.domain.order.payment.PaymentStatus;
import com.kopchak.worldoftoys.service.OrderService;
import com.kopchak.worldoftoys.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin
@RequiredArgsConstructor
@Tag(name = "admin-panel-controller", description = "")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminPanelController {

    private final ProductService productService;
    private final OrderService orderService;

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
                    responseCode = "400",
                    description = "The product image cannot be decompressed",
                    content = @Content(schema = @Schema(implementation = ResponseStatusExceptionDto.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product with this id is not found",
                    content = @Content(schema = @Schema(implementation = ResponseStatusExceptionDto.class)))
    })
    @GetMapping("/products/{productId}")
    public ResponseEntity<AdminProductDto> getAdminProductDtoById(@PathVariable(name = "productId") Integer productId) {
        try {
            AdminProductDto product = productService.getAdminProductDtoById(productId);
            return new ResponseEntity<>(product, HttpStatus.OK);
        } catch (ImageDecompressionException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (ProductNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Operation(summary = "Update product")
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
        } catch (ProductNotFoundException | InvalidCategoryTypeException | InvalidImageFileFormatException |
                 ImageExceedsMaxSizeException | ImageCompressionException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Add product")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Product was successfully created",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid product data",
                    content = @Content(schema = @Schema(implementation = ResponseStatusExceptionDto.class)))
    })
    @PostMapping("/products/add")
    public ResponseEntity<Void> addProduct(@Valid @RequestPart("product") AddUpdateProductDto addUpdateProductDto,
                                           @RequestPart("image") MultipartFile mainImageFile,
                                           @RequestPart("images") List<MultipartFile> imageFilesList) {
        try {
            productService.addProduct(addUpdateProductDto, mainImageFile, imageFilesList);
        } catch (ProductNotFoundException | InvalidCategoryTypeException | InvalidImageFileFormatException |
                 ImageExceedsMaxSizeException | ImageCompressionException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "Delete product")
    @ApiResponse(
            responseCode = "204",
            description = "Product was successfully deleted",
            content = @Content(schema = @Schema(hidden = true)))
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable(name = "productId") Integer productId) {
        productService.deleteProduct(productId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Get all categories")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Product categories were successfully fetched",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(
                                            schema = @Schema(implementation = AdminCategoryDto.class))
                            )
                    }),
            @ApiResponse(
                    responseCode = "400",
                    description = "Category type is invalid",
                    content = @Content(schema = @Schema(implementation = ResponseStatusExceptionDto.class)))
    })
    @GetMapping("/categories/{categoryType}")
    public ResponseEntity<Set<AdminCategoryDto>> getProductCategories(
            @PathVariable("categoryType") String categoryType) {
        try {
            Set<AdminCategoryDto> categoryDtoSet = productService.getAdminCategories(categoryType);
            return new ResponseEntity<>(categoryDtoSet, HttpStatus.OK);
        } catch (InvalidCategoryTypeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(summary = "Delete product category")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Product category was successfully deleted",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Product category is incorrect",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/categories/{categoryType}/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("categoryType") String categoryType,
                                               @PathVariable(name = "categoryId") Integer categoryId) {
        try {
            productService.deleteCategory(categoryType, categoryId);
        } catch (InvalidCategoryTypeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/categories/{categoryType}/{categoryId}")
    public ResponseEntity<Void> updateCategory(@PathVariable("categoryType") String categoryType,
                                               @PathVariable(name = "categoryId") Integer categoryId,
                                               @Valid @RequestBody CategoryNameDto categoryNameDto) {
        try {
            productService.updateCategory(categoryType, categoryId, categoryNameDto);
        } catch (InvalidCategoryTypeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/categories/{categoryType}/add")
    public ResponseEntity<Void> addCategory(@PathVariable("categoryType") String categoryType,
                                            @Valid @RequestBody CategoryNameDto categoryNameDto) {
        try {
            productService.addCategory(categoryType, categoryNameDto);
        } catch (InvalidCategoryTypeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/orders/filtering-options")
    public ResponseEntity<FilteringOrderOptionsDto> getOrderFilteringOptions() {
        return new ResponseEntity<>(orderService.getOrderFilteringOptions(), HttpStatus.OK);
    }

    @GetMapping("/orders")
    public ResponseEntity<FilteredOrdersPageDto> filterOrdersByStatusesAndDate(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "order-status", required = false) List<OrderStatus> orderStatuses,
            @RequestParam(name = "payment-status", required = false) List<PaymentStatus> paymentStatuses,
            @RequestParam(name = "date-sort", required = false) String dateSortOrder
    ) {
        FilteredOrdersPageDto filteredOrdersPageDto =
                orderService.filterOrdersByStatusesAndDate(page, size, orderStatuses, paymentStatuses, dateSortOrder);
        return new ResponseEntity<>(filteredOrdersPageDto, HttpStatus.OK);
    }

    //ToDo: add method which return all OrderStatuses
    @PatchMapping("/orders/{orderId}")
    public ResponseEntity<Void> updateOrderStatus(@PathVariable(name = "orderId") String orderId,
                                                  @RequestBody StatusDto statusDto) {
        try {
            orderService.updateOrderStatus(orderId, statusDto);
        } catch (OrderCreationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
