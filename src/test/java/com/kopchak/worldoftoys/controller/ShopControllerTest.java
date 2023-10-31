package com.kopchak.worldoftoys.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kopchak.worldoftoys.dto.error.ErrorResponseDto;
import com.kopchak.worldoftoys.dto.product.FilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.product.ProductDto;
import com.kopchak.worldoftoys.dto.product.category.FilteringProductCategoriesDto;
import com.kopchak.worldoftoys.dto.product.category.ProductCategoryDto;
import com.kopchak.worldoftoys.service.JwtTokenService;
import com.kopchak.worldoftoys.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(controllers = ShopController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class ShopControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private JwtTokenService jwtTokenService;

    @Autowired
    private ObjectMapper objectMapper;

    private String productName;
    private BigDecimal minProductPrice;
    private BigDecimal maxProductPrice;
    private List<String> originCategories;
    private List<String> brandCategories;
    private List<String> ageCategories;
    private MultiValueMap<String, String> requestProductFilteringParams;

    @BeforeEach
    void setUp() {
        productName = "Лялька";
        minProductPrice = BigDecimal.valueOf(350);
        maxProductPrice = BigDecimal.valueOf(1000);
        originCategories = List.of("china", "ukraine");
        brandCategories = List.of("сurlimals", "devilon");
        ageCategories = List.of("do-1-roku", "vid-1-do-3-rokiv");

        requestProductFilteringParams = new LinkedMultiValueMap<>() {{
            add("name", productName);
            add("min-price", minProductPrice.toString());
            add("max-price", maxProductPrice.toString());
            add("origin", String.join(",", originCategories));
            add("brand", String.join(",", brandCategories));
            add("age", String.join(",", ageCategories));
        }};
    }

    @Test
    public void getFilteredProducts_RequestFilteringParams_ReturnsOkStatusAndFilteredProductsPageDto() throws Exception {
        int page = 0;
        int size = 10;
        String priceAscSortOrder = "asc";
        requestProductFilteringParams.add("page", String.valueOf(page));
        requestProductFilteringParams.add("size", String.valueOf(size));
        requestProductFilteringParams.add("price-sort", priceAscSortOrder);
        FilteredProductsPageDto expectedFilteredProductsPageDto = new FilteredProductsPageDto(new ArrayList<>(),
                20, 15);

        when(productService.getFilteredProducts(eq(page), eq(size), eq(productName), eq(minProductPrice),
                eq(maxProductPrice), eq(originCategories), eq(brandCategories), eq(ageCategories),
                eq(priceAscSortOrder))).thenReturn(expectedFilteredProductsPageDto);

        ResultActions response = mockMvc.perform(get("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .params(requestProductFilteringParams));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedFilteredProductsPageDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void getFilteringProductCategories_RequestFilteringParams_ReturnsOkStatusAndFilteringProductCategoriesDto() throws Exception {
        var expectedFilteringProductCategoriesDto = FilteringProductCategoriesDto
                .builder()
                .originCategories(List.of(new ProductCategoryDto("Китай", "china")))
                .brandCategories(List.of(new ProductCategoryDto("Devilon", "devilon"),
                        new ProductCategoryDto("Сurlimals", "сurlimals")))
                .ageCategories(List.of(new ProductCategoryDto("від 1 до 3 років", "vid-1-do-3-rokiv")))
                .build();

        when(productService.getFilteringProductCategories(eq(productName), eq(minProductPrice), eq(maxProductPrice),
                eq(originCategories), eq(brandCategories), eq(ageCategories)))
                .thenReturn(expectedFilteringProductCategoriesDto);

        ResultActions response = mockMvc.perform(get("/api/v1/products/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .params(requestProductFilteringParams));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedFilteringProductCategoriesDto)))
                .andDo(MockMvcResultHandlers.print());
    }


    @Test
    public void getProductBySlug_NonExistentProductSlug_ReturnsNotFoundStatusAndErrorResponseDto() throws Exception {
        String nonExistentProductSlug = "non-existent-product-slug";
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.name(), "Product doesn't exist");

        when(productService.getProductDtoBySlug(eq(nonExistentProductSlug))).thenReturn(Optional.empty());

        ResultActions response = mockMvc.perform(get("/api/v1/products/{productSlug}", nonExistentProductSlug)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().json(objectMapper.writeValueAsString(errorResponseDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void getProductBySlug_ExistentProductSlug_ReturnsOkStatusAndProductDto() throws Exception {
        String existentProductSlug = "lyalka-darynka";

        ProductDto expectedProductDto = ProductDto
                .builder()
                .name("Лялька Даринка")
                .slug("lyalka-darynka")
                .price(BigDecimal.valueOf(900))
                .availableQuantity(BigInteger.valueOf(200))
                .build();

        when(productService.getProductDtoBySlug(eq(existentProductSlug))).thenReturn(Optional.of(expectedProductDto));

        ResultActions response = mockMvc.perform(get("/api/v1/products/{productSlug}", existentProductSlug)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedProductDto)))
                .andDo(MockMvcResultHandlers.print());
    }
}