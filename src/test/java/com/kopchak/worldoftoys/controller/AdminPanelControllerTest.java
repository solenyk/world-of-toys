package com.kopchak.worldoftoys.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kopchak.worldoftoys.dto.admin.category.CategoryIdDto;
import com.kopchak.worldoftoys.dto.admin.product.AddUpdateProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminProductsPageDto;
import com.kopchak.worldoftoys.dto.error.ResponseStatusExceptionDto;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
    private final static String PRODUCT_NAME = "лялька";
    private AdminProductDto adminProductDto;
    private CategoryIdDto categoryIdDto;
    private AddUpdateProductDto addUpdateProductDto;
    private String addUpdateProductDtoJson;
    private MockMultipartFile addUpdateProductDtoJsonFile;
    private MockMultipartFile mainImage;

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
        String duplicateProductNameExceptionMsg =
                String.format("The product with name: %s is already exist", PRODUCT_NAME);
        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.BAD_REQUEST,
                duplicateProductNameExceptionMsg);


        doThrow(new DuplicateProductNameException(duplicateProductNameExceptionMsg))
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

    private ResponseStatusExceptionDto getResponseStatusExceptionDto(HttpStatus httpStatus, String msg) {
        return ResponseStatusExceptionDto
                .builder()
                .error(httpStatus.name())
                .status(httpStatus.value())
                .message(msg)
                .build();
    }
}