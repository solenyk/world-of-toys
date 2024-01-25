package com.kopchak.worldoftoys.controller;

import com.kopchak.worldoftoys.domain.order.OrderStatus;
import com.kopchak.worldoftoys.domain.order.payment.PaymentStatus;
import com.kopchak.worldoftoys.domain.product.category.type.CategoryType;
import com.kopchak.worldoftoys.dto.admin.category.AdminCategoryDto;
import com.kopchak.worldoftoys.dto.admin.category.CategoryNameDto;
import com.kopchak.worldoftoys.dto.admin.order.FilteredOrdersPageDto;
import com.kopchak.worldoftoys.dto.admin.order.FilteringOrderOptionsDto;
import com.kopchak.worldoftoys.dto.admin.order.StatusDto;
import com.kopchak.worldoftoys.dto.admin.product.AddUpdateProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminProductsPageDto;
import com.kopchak.worldoftoys.dto.error.ResponseStatusExceptionDto;
import com.kopchak.worldoftoys.dto.product.FilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.product.ProductDto;
import com.kopchak.worldoftoys.exception.exception.category.CategoryContainsProductsException;
import com.kopchak.worldoftoys.exception.exception.category.CategoryCreationException;
import com.kopchak.worldoftoys.exception.exception.category.CategoryNotFoundException;
import com.kopchak.worldoftoys.exception.exception.category.DuplicateCategoryNameException;
import com.kopchak.worldoftoys.exception.exception.email.MessageSendingException;
import com.kopchak.worldoftoys.exception.exception.image.ImageException;
import com.kopchak.worldoftoys.exception.exception.image.ext.ImageDecompressionException;
import com.kopchak.worldoftoys.exception.exception.order.InvalidOrderException;
import com.kopchak.worldoftoys.exception.exception.order.InvalidOrderStatusException;
import com.kopchak.worldoftoys.exception.exception.product.DuplicateProductNameException;
import com.kopchak.worldoftoys.exception.exception.product.ProductNotFoundException;
import com.kopchak.worldoftoys.service.CategoryService;
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
@Tag(name = "admin-panel-controller", description = "The admin panel controller manages administrative tasks related " +
        "to products and orders. It provides endpoints for filtering and managing products, categories, and orders.")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminPanelController {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;

    @Operation(summary = "Fetch filtered products")
    @ApiResponse(
            responseCode = "200",
            description = "Products were successfully fetched",
            content = @Content(schema = @Schema(implementation = FilteredProductsPageDto.class)))
    @GetMapping("/products")
    public ResponseEntity<AdminProductsPageDto> getFilteredProductsPage(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "name", required = false) String productName,
            @RequestParam(name = "min-price", required = false) BigDecimal minPrice,
            @RequestParam(name = "max-price", required = false) BigDecimal maxPrice,
            @RequestParam(name = "origin", required = false) List<String> originCategories,
            @RequestParam(name = "brand", required = false) List<String> brandCategories,
            @RequestParam(name = "age", required = false) List<String> ageCategories,
            @RequestParam(name = "price-sort", required = false) String priceSortOrder,
            @RequestParam(name = "availability", required = false) String availability

    ) {
        var productsPage = productService.getAdminProductsPage(page, size, productName, minPrice, maxPrice,
                originCategories, brandCategories, ageCategories, priceSortOrder, availability);
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
    public ResponseEntity<AdminProductDto> getProductById(@PathVariable(name = "productId") Integer productId) {
        try {
            AdminProductDto product = productService.getProductById(productId);
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
                    content = @Content(schema = @Schema(implementation = ResponseStatusExceptionDto.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product or category is not found",
                    content = @Content(schema = @Schema(implementation = ResponseStatusExceptionDto.class)))
    })
    @PutMapping("/products/{productId}")
    public ResponseEntity<Void> updateProduct(
            @PathVariable(name = "productId") Integer productId,
            @Valid @RequestPart("product") AddUpdateProductDto addUpdateProductDto,
            @RequestPart(value = "image", required = false) MultipartFile mainImageFile,
            @RequestPart(value = "images", required = false) List<MultipartFile> imageFilesList) {
        try {
            productService.updateProduct(productId, addUpdateProductDto, mainImageFile, imageFilesList);
        } catch (ImageException | DuplicateProductNameException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (ProductNotFoundException | CategoryNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Create product")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Product was successfully created",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid product data",
                    content = @Content(schema = @Schema(implementation = ResponseStatusExceptionDto.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product category is not found",
                    content = @Content(schema = @Schema(implementation = ResponseStatusExceptionDto.class)))
    })
    @PostMapping("/products/add")
    public ResponseEntity<Void> createProduct(
            @Valid @RequestPart("product") AddUpdateProductDto addUpdateProductDto,
            @RequestPart(value = "image", required = false) MultipartFile mainImageFile,
            @RequestPart(value = "images", required = false) List<MultipartFile> imageFilesList) {
        try {
            productService.createProduct(addUpdateProductDto, mainImageFile, imageFilesList);
        } catch (DuplicateProductNameException | ImageException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (CategoryNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "Get all categories")
    @ApiResponse(
            responseCode = "200",
            description = "Product categories were successfully fetched",
            content = {
                    @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = AdminCategoryDto.class))
                    )
            })
    @GetMapping("/categories/{categoryType}")
    public ResponseEntity<Set<AdminCategoryDto>> getProductCategories(
            @PathVariable("categoryType") CategoryType categoryType) {
        return new ResponseEntity<>(categoryService.getAdminCategories(categoryType), HttpStatus.OK);
    }

    @Operation(summary = "Delete product category")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Product category was successfully deleted",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Product category contains products",
                    content = @Content(schema = @Schema(implementation = ResponseStatusExceptionDto.class)))
    })
    @DeleteMapping("/categories/{categoryType}/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("categoryType") CategoryType categoryType,
                                               @PathVariable(name = "categoryId") Integer categoryId) {
        try {
            categoryService.deleteCategory(categoryType, categoryId);
        } catch (CategoryContainsProductsException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Update product category")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Product category was successfully updated",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Product category name already exist",
                    content = @Content(schema = @Schema(implementation = ResponseStatusExceptionDto.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product category is not found",
                    content = @Content(schema = @Schema(implementation = ResponseStatusExceptionDto.class)))
    })
    @PutMapping("/categories/{categoryType}/{categoryId}")
    public ResponseEntity<Void> updateCategory(@PathVariable("categoryType") CategoryType categoryType,
                                               @PathVariable(name = "categoryId") Integer categoryId,
                                               @Valid @RequestBody CategoryNameDto categoryNameDto) {
        try {
            categoryService.updateCategory(categoryType, categoryId, categoryNameDto);
        } catch (DuplicateCategoryNameException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (CategoryNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Create product category")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Product category was successfully created",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Product category type is incorrect",
                    content = @Content(schema = @Schema(implementation = ResponseStatusExceptionDto.class)))
    })
    @PostMapping("/categories/{categoryType}/add")
    public ResponseEntity<Void> createCategory(@PathVariable("categoryType") CategoryType categoryType,
                                               @Valid @RequestBody CategoryNameDto categoryNameDto) {
        try {
            categoryService.createCategory(categoryType, categoryNameDto);
        } catch (DuplicateCategoryNameException | CategoryCreationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "Get order filtering options")
    @ApiResponse(
            responseCode = "200",
            description = "Order filtering options were successfully fetched",
            content = @Content(schema = @Schema(implementation = FilteringOrderOptionsDto.class)))
    @GetMapping("/orders/filtering-options")
    public ResponseEntity<FilteringOrderOptionsDto> getOrderFilteringOptions() {
        return new ResponseEntity<>(orderService.getOrderFilteringOptions(), HttpStatus.OK);
    }

    @Operation(summary = "Fetch filtered orders")
    @ApiResponse(
            responseCode = "200",
            description = "Filtered orders were successfully fetched",
            content = @Content(schema = @Schema(implementation = FilteredOrdersPageDto.class)))
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

    @Operation(summary = "Get order statuses")
    @ApiResponse(
            responseCode = "200",
            description = "Order statuses were successfully fetched",
            content = {
                    @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = StatusDto.class))
                    )
            })
    @GetMapping("/orders/statuses")
    public ResponseEntity<Set<StatusDto>> getAllOrderStatuses() {
        return new ResponseEntity<>(orderService.getAllOrderStatuses(), HttpStatus.OK);
    }

    @Operation(summary = "Update order status")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Order status was successfully updated",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid order id or status",
                    content = @Content(schema = @Schema(implementation = ResponseStatusExceptionDto.class))),
            @ApiResponse(
                    responseCode = "503",
                    description = "Service unavailable",
                    content = @Content(schema = @Schema(implementation = ResponseStatusExceptionDto.class)))
    })
    @PatchMapping("/orders/{orderId}")
    public ResponseEntity<Void> updateOrderStatus(@PathVariable(name = "orderId") String orderId,
                                                  @Valid @RequestBody StatusDto statusDto) {
        try {
            orderService.updateOrderStatus(orderId, statusDto);
        } catch (InvalidOrderStatusException | InvalidOrderException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (MessageSendingException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
