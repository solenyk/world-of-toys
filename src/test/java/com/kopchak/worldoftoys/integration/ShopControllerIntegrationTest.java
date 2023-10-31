package com.kopchak.worldoftoys.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kopchak.worldoftoys.dto.product.FilteredProductDto;
import com.kopchak.worldoftoys.dto.product.FilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.product.category.FilteringProductCategoriesDto;
import com.kopchak.worldoftoys.dto.product.category.ProductCategoryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("integrationtest")
public class ShopControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    private MultiValueMap<String, String> requestProductFilteringParams;

    @BeforeEach
    void setUp() {
        requestProductFilteringParams = new LinkedMultiValueMap<>(){{
            add("name", "Лялька");
            add("min-price", "350");
            add("max-price", "1000");
            add("origin", "china,ukraine");
            add("brand", "сurlimals,devilon");
            add("age", "do-1-roku,vid-1-do-3-rokiv");
        }};
    }

    @Test
    public void getFilteredProducts_RequestFilteringParams_ReturnsOkStatusAndFilteredProductsPageDto() throws Exception {
        requestProductFilteringParams.add("page", "0");
        requestProductFilteringParams.add("size", "10");
        requestProductFilteringParams.add("price-sort", "asc");

        List<FilteredProductDto> expectedFilteredProductsPageDtoContent = new ArrayList<>(){{
            add(new FilteredProductDto("Лялька Русалочка", "lyalka-rusalochka", BigDecimal.valueOf(550),
                    BigInteger.valueOf(150), null));
            add(new FilteredProductDto("Лялька Даринка", "lyalka-darynka", BigDecimal.valueOf(900),
                    BigInteger.valueOf(200), null));
        }};
        FilteredProductsPageDto expectedFilteredProductsPageDto = new FilteredProductsPageDto(
                expectedFilteredProductsPageDtoContent, 2, 1);

        ResultActions response = mockMvc.perform(get("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .params(requestProductFilteringParams));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedFilteredProductsPageDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void getFilteringProductCategories_RequestFilteringParams_ReturnsOkStatusAndFilteringProductCategoriesDto() throws Exception {
        List<ProductCategoryDto> expectedBrandCategories = new ArrayList<>(){{
            add(new ProductCategoryDto("Devilon", "devilon"));
            add(new ProductCategoryDto("Сurlimals", "сurlimals"));
        }};
        List<ProductCategoryDto> expectedOriginCategories = new ArrayList<>(){{
            add(new ProductCategoryDto("Китай", "china"));
            add(new ProductCategoryDto("Україна", "ukraine"));
        }};
        List<ProductCategoryDto> expectedAgeCategories = new ArrayList<>(){{
            add(new ProductCategoryDto("від 1 до 3 років", "vid-1-do-3-rokiv"));
            add(new ProductCategoryDto("від 6 до 9 років", "vid-6-do-9-rokiv"));
        }};
        var expectedFilteringProductCategoriesDto = FilteringProductCategoriesDto
                .builder()
                .originCategories(expectedOriginCategories)
                .brandCategories(expectedBrandCategories)
                .ageCategories(expectedAgeCategories)
                .build();

        ResultActions response = mockMvc.perform(get("/api/v1/products/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .params(requestProductFilteringParams));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedFilteringProductCategoriesDto)))
                .andDo(MockMvcResultHandlers.print());
    }
}
