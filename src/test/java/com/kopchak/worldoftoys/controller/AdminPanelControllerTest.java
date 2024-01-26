package com.kopchak.worldoftoys.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kopchak.worldoftoys.domain.order.OrderStatus;
import com.kopchak.worldoftoys.domain.order.payment.PaymentStatus;
import com.kopchak.worldoftoys.domain.product.category.type.CategoryType;
import com.kopchak.worldoftoys.dto.admin.category.AdminCategoryDto;
import com.kopchak.worldoftoys.dto.admin.category.CategoryIdDto;
import com.kopchak.worldoftoys.dto.admin.category.CategoryNameDto;
import com.kopchak.worldoftoys.dto.admin.order.FilteredOrdersPageDto;
import com.kopchak.worldoftoys.dto.admin.order.FilteringOrderOptionsDto;
import com.kopchak.worldoftoys.dto.admin.order.StatusDto;
import com.kopchak.worldoftoys.dto.admin.product.AddUpdateProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminProductsPageDto;
import com.kopchak.worldoftoys.dto.error.ResponseStatusExceptionDto;
import com.kopchak.worldoftoys.exception.exception.category.CategoryContainsProductsException;
import com.kopchak.worldoftoys.exception.exception.category.CategoryNotFoundException;
import com.kopchak.worldoftoys.exception.exception.category.DuplicateCategoryNameException;
import com.kopchak.worldoftoys.exception.exception.image.ext.ImageDecompressionException;
import com.kopchak.worldoftoys.exception.exception.product.DuplicateProductNameException;
import com.kopchak.worldoftoys.exception.exception.product.ProductNotFoundException;
import com.kopchak.worldoftoys.service.CategoryService;
import com.kopchak.worldoftoys.service.JwtTokenService;
import com.kopchak.worldoftoys.service.OrderService;
import com.kopchak.worldoftoys.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(controllers = AdminPanelController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class AdminPanelControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private OrderService orderService;

    @MockBean
    private JwtTokenService jwtTokenService;

    @Autowired
    private ObjectMapper objectMapper;
    private final static Integer PRODUCT_ID = 1;
    private final static Integer CATEGORY_ID = 1;
    private final static String PRODUCT_NAME = "лялька";
    private final static String CATEGORY_NAME = "name";
    private final static CategoryType CATEGORY_TYPE = CategoryType.BRANDS;
    private final static String DUPLICATE_PRODUCT_NAME_EXCEPTION_MSG =
            String.format("The product with name: %s is already exist", PRODUCT_NAME);
    private final static String DUPLICATE_CATEGORY_NAME_EXCEPTION_MSG =
            String.format("Category with name: %s already exist", CATEGORY_NAME);
    private AdminProductDto adminProductDto;
    private CategoryIdDto categoryIdDto;
    private AddUpdateProductDto addUpdateProductDto;
    private String addUpdateProductDtoJson;
    private MockMultipartFile addUpdateProductDtoJsonFile;
    private MockMultipartFile mainImage;
    private CategoryNameDto categoryNameDto;
    private StatusDto statusDto;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        adminProductDto = AdminProductDto
                .builder()
                .id(PRODUCT_ID)
                .name(PRODUCT_NAME)
                .build();
        categoryIdDto = new CategoryIdDto(1);
        addUpdateProductDto = AddUpdateProductDto
                .builder()
                .name(PRODUCT_NAME)
                .description("description")
                .price(BigDecimal.TEN)
                .availableQuantity(BigInteger.ONE)
                .isAvailable(true)
                .brandCategory(categoryIdDto)
                .originCategory(categoryIdDto)
                .ageCategories(List.of(categoryIdDto))
                .build();
        addUpdateProductDtoJson = objectMapper.writeValueAsString(addUpdateProductDto);
        addUpdateProductDtoJsonFile = new MockMultipartFile("product", null,
                "application/json", addUpdateProductDtoJson.getBytes());
        mainImage = new MockMultipartFile("image", "filename",
                "image/jpg", "image".getBytes());
        categoryNameDto = new CategoryNameDto(CATEGORY_NAME);
        statusDto = new StatusDto("name", "status");
    }

    @Test
    public void getProductById_ReturnsOkStatus() throws Exception {
        when(productService.getProductById(PRODUCT_ID)).thenReturn(adminProductDto);

        ResultActions response = mockMvc.perform(get("/api/v1/admin/products/{productId}", PRODUCT_ID)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(adminProductDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void getProductById_ThrowImageDecompressionException_ReturnsBadRequestStatusAndResponseStatusExceptionDto() throws Exception {
        String imageName = "image.jpg";
        String imageDecompressionExceptionMsg =
                String.format("The image with name: %s cannot be decompressed", imageName);
        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.BAD_REQUEST, imageDecompressionExceptionMsg);

        doThrow(new ImageDecompressionException(imageDecompressionExceptionMsg))
                .when(productService).getProductById(eq(PRODUCT_ID));

        ResultActions response = mockMvc.perform(get("/api/v1/admin/products/{productId}", PRODUCT_ID)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void getProductById_ThrowProductNotFoundException_ReturnsNotFoundStatusAndResponseStatusExceptionDto() throws Exception {
        String productNotFoundExceptionMsg = String.format("The product with id: %d is not found.", PRODUCT_ID);
        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.NOT_FOUND, productNotFoundExceptionMsg);

        doThrow(new ProductNotFoundException(productNotFoundExceptionMsg))
                .when(productService).getProductById(PRODUCT_ID);

        ResultActions response = mockMvc.perform(get("/api/v1/admin/products/{productId}", PRODUCT_ID)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void getFilteredProductsPage_ReturnsOkStatus() throws Exception {
        List<String> originCategories = List.of("china", "ukraine");
        List<String> brandCategories = List.of("сurlimals", "devilon");
        List<String> ageCategories = List.of("do-1-roku", "vid-1-do-3-rokiv");
        String priceSortOrder = "asc";
        String availability = "available";
        var adminProductsPageDto = new AdminProductsPageDto(new ArrayList<>(), 0L, 0L);

        when(productService.getAdminProductsPage(eq(0), eq(1), eq(PRODUCT_NAME), any(), any(),
                eq(originCategories), eq(brandCategories), eq(ageCategories), eq(priceSortOrder), eq(availability)))
                .thenReturn(adminProductsPageDto);

        ResultActions response = mockMvc.perform(get("/api/v1/admin/products")
                .param("page", "0")
                .param("size", "1")
                .param("name", PRODUCT_NAME)
                .param("origin", "china", "ukraine")
                .param("brand", "сurlimals", "devilon")
                .param("age", "do-1-roku", "vid-1-do-3-rokiv")
                .param("price-sort", priceSortOrder)
                .param("availability", availability)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(adminProductsPageDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void updateProduct_ReturnsNoContentStatus() throws Exception {
        doNothing().when(productService).updateProduct(eq(PRODUCT_ID), eq(addUpdateProductDto), eq(mainImage), any());

        ResultActions response = mockMvc
                .perform(multipart(HttpMethod.PUT, "/api/v1/admin/products/{productId}", PRODUCT_ID)
                        .file(addUpdateProductDtoJsonFile)
                        .file(mainImage));

        response.andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void updateProduct_ThrowDuplicateProductNameException_ReturnsBadRequestStatusAndResponseStatusExceptionDto() throws Exception {
        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.BAD_REQUEST,
                DUPLICATE_PRODUCT_NAME_EXCEPTION_MSG);


        doThrow(new DuplicateProductNameException(DUPLICATE_PRODUCT_NAME_EXCEPTION_MSG))
                .when(productService).updateProduct(eq(PRODUCT_ID), eq(addUpdateProductDto), eq(mainImage), any());

        ResultActions response = mockMvc
                .perform(multipart(HttpMethod.PUT, "/api/v1/admin/products/{productId}", PRODUCT_ID)
                        .file(addUpdateProductDtoJsonFile)
                        .file(mainImage));

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void updateProduct_ThrowProductNotFoundException_ReturnsNotFoundStatusAndResponseStatusExceptionDto() throws Exception {
        String productNotFoundExceptionMsg =
                String.format("The product with name: %s is already exist", PRODUCT_NAME);
        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.NOT_FOUND,
                productNotFoundExceptionMsg);


        doThrow(new ProductNotFoundException(productNotFoundExceptionMsg))
                .when(productService).updateProduct(eq(PRODUCT_ID), eq(addUpdateProductDto), eq(mainImage), any());

        ResultActions response = mockMvc
                .perform(multipart(HttpMethod.PUT, "/api/v1/admin/products/{productId}", PRODUCT_ID)
                        .file(addUpdateProductDtoJsonFile)
                        .file(mainImage));

        response.andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void createProduct_ReturnsCreatedStatus() throws Exception {
        doNothing().when(productService).createProduct(eq(addUpdateProductDto), eq(mainImage), any());

        ResultActions response = mockMvc
                .perform(multipart(HttpMethod.POST, "/api/v1/admin/products/add")
                        .file(addUpdateProductDtoJsonFile)
                        .file(mainImage));

        response.andExpect(MockMvcResultMatchers.status().isCreated())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void createProduct_ThrowDuplicateProductNameException_ReturnsBadRequestStatusAndResponseStatusExceptionDto() throws Exception {
        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.BAD_REQUEST,
                DUPLICATE_PRODUCT_NAME_EXCEPTION_MSG);


        doThrow(new DuplicateProductNameException(DUPLICATE_PRODUCT_NAME_EXCEPTION_MSG))
                .when(productService).createProduct(eq(addUpdateProductDto), eq(mainImage), any());

        ResultActions response = mockMvc
                .perform(multipart(HttpMethod.POST, "/api/v1/admin/products/add")
                        .file(addUpdateProductDtoJsonFile)
                        .file(mainImage));

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void createProduct_ThrowCategoryNotFoundException_ReturnsNotFoundStatusAndResponseStatusExceptionDto() throws Exception {
        String categoryNotFoundExceptionMsg = String.format("The category with id: %d is not found", 1);
        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.NOT_FOUND,
                categoryNotFoundExceptionMsg);


        doThrow(new CategoryNotFoundException(categoryNotFoundExceptionMsg))
                .when(productService).createProduct(eq(addUpdateProductDto), eq(mainImage), any());

        ResultActions response = mockMvc
                .perform(multipart(HttpMethod.POST, "/api/v1/admin/products/add")
                        .file(addUpdateProductDtoJsonFile)
                        .file(mainImage));

        response.andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void getProductCategories_ReturnsOkStatusAndAdminCategoryDtoSet() throws Exception {
        AdminCategoryDto adminCategoryDto = new AdminCategoryDto(CATEGORY_ID, "name");
        Set<AdminCategoryDto> adminCategoryDtoSet = Set.of(adminCategoryDto);

        when(categoryService.getAdminCategories(eq(CATEGORY_TYPE))).thenReturn(adminCategoryDtoSet);

        ResultActions response = mockMvc
                .perform(get("/api/v1/admin/categories/{categoryType}", "brands")
                        .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(adminCategoryDtoSet)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void deleteCategory_ReturnsNoContentStatus() throws Exception {
        doNothing().when(categoryService).deleteCategory(eq(CATEGORY_TYPE), eq(CATEGORY_ID));

        ResultActions response = mockMvc
                .perform(delete("/api/v1/admin/categories/{categoryType}/{categoryId}",
                        "brands", CATEGORY_ID)
                        .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void deleteCategory_ThrowCategoryContainsProductsException_ReturnsBadRequestStatusAndResponseStatusExceptionDto() throws Exception {
        String categoryContainsProductsExceptionMsg = String.format("It is not possible to delete a category " +
                "with id: %d because there are products in this category.", CATEGORY_ID);
        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.BAD_REQUEST,
                categoryContainsProductsExceptionMsg);


        doThrow(new CategoryContainsProductsException(categoryContainsProductsExceptionMsg))
                .when(categoryService).deleteCategory(eq(CATEGORY_TYPE), eq(CATEGORY_ID));

        ResultActions response = mockMvc
                .perform(delete("/api/v1/admin/categories/{categoryType}/{categoryId}",
                        "brands", CATEGORY_ID)
                        .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void updateCategory_ReturnsNoContentStatus() throws Exception {
        doNothing().when(categoryService).updateCategory(eq(CATEGORY_TYPE), eq(CATEGORY_ID), eq(categoryNameDto));

        ResultActions response = mockMvc
                .perform(put("/api/v1/admin/categories/{categoryType}/{categoryId}",
                        "brands", CATEGORY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryNameDto)));

        response.andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void updateCategory_ThrowDuplicateCategoryNameException_ReturnsBadRequestStatusAndResponseStatusExceptionDto() throws Exception {
        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.BAD_REQUEST,
                DUPLICATE_CATEGORY_NAME_EXCEPTION_MSG);


        doThrow(new DuplicateCategoryNameException(DUPLICATE_CATEGORY_NAME_EXCEPTION_MSG))
                .when(categoryService).updateCategory(eq(CATEGORY_TYPE), eq(CATEGORY_ID), eq(categoryNameDto));

        ResultActions response = mockMvc
                .perform(put("/api/v1/admin/categories/{categoryType}/{categoryId}",
                        "brands", CATEGORY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryNameDto)));

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void createCategory_ReturnsCreatedStatus() throws Exception {
        doNothing().when(categoryService).createCategory(eq(CATEGORY_TYPE), eq(categoryNameDto));

        ResultActions response = mockMvc
                .perform(post("/api/v1/admin/categories/{categoryType}/add", "brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryNameDto)));

        response.andExpect(MockMvcResultMatchers.status().isCreated())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void createCategory_ThrowDuplicateCategoryNameException_ReturnsBadRequestStatusAndResponseStatusExceptionDto() throws Exception {
        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.BAD_REQUEST,
                DUPLICATE_CATEGORY_NAME_EXCEPTION_MSG);

        doThrow(new DuplicateCategoryNameException(DUPLICATE_CATEGORY_NAME_EXCEPTION_MSG))
                .when(categoryService).createCategory(eq(CATEGORY_TYPE), eq(categoryNameDto));

        ResultActions response = mockMvc
                .perform(post("/api/v1/admin/categories/{categoryType}/add", "brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryNameDto)));

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void getOrderFilteringOptions_ReturnsOkStatus() throws Exception {
        var filteringOrderOptionsDto = new FilteringOrderOptionsDto(Set.of(statusDto), Set.of(statusDto));

        when(orderService.getOrderFilteringOptions()).thenReturn(filteringOrderOptionsDto);

        ResultActions response = mockMvc.perform(get("/api/v1/admin/orders/filtering-options")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryNameDto)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(filteringOrderOptionsDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void filterOrdersByStatusesAndDate_ReturnsOkStatus() throws Exception {
        List<OrderStatus> orderStatuses = List.of(OrderStatus.CANCELED, OrderStatus.SHIPPED);
        List<PaymentStatus> paymentStatuses = List.of(PaymentStatus.FAILED);
        String dateSortOrder = "asc";
        var filteredOrdersPageDto = new FilteredOrdersPageDto(new HashSet<>(), 0L, 0L);

        when(orderService.filterOrdersByStatusesAndDate(eq(0), eq(1), eq(orderStatuses), eq(paymentStatuses),
                eq(dateSortOrder))).thenReturn(filteredOrdersPageDto);

        ResultActions response = mockMvc.perform(get("/api/v1/admin/orders")
                .param("page", "0")
                .param("size", "1")
                .param("order-status", OrderStatus.CANCELED.name(), OrderStatus.SHIPPED.name())
                .param("payment-status", PaymentStatus.FAILED.name())
                .param("date-sort", dateSortOrder)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(filteredOrdersPageDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void getAllOrderStatuses_ReturnsOkStatus() throws Exception {
        Set<StatusDto> statusDtoSet = Set.of(statusDto);

        when(orderService.getAllOrderStatuses()).thenReturn(statusDtoSet);

        ResultActions response = mockMvc.perform(get("/api/v1/admin/orders/statuses")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(statusDtoSet)))
                .andDo(MockMvcResultHandlers.print());
    }

    private ResponseStatusExceptionDto getResponseStatusExceptionDto(HttpStatus httpStatus, String msg) {
        return ResponseStatusExceptionDto
                .builder()
                .error(httpStatus.name())
                .status(httpStatus.value())
                .message(msg)
                .build();
    }
}