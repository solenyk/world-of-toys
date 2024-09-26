package com.kopchak.worldoftoys.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kopchak.worldoftoys.dto.error.ExceptionDto;
import com.kopchak.worldoftoys.dto.product.FilteredProductDto;
import com.kopchak.worldoftoys.dto.product.FilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.product.ProductDto;
import com.kopchak.worldoftoys.dto.product.category.CategoryDto;
import com.kopchak.worldoftoys.dto.product.category.FilteringCategoriesDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
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
        requestProductFilteringParams = new LinkedMultiValueMap<>() {{
            add("name", "Лялька");
            add("min-price", "350");
            add("max-price", "1000");
            add("origin", "china,ukraine");
            add("brand", "сurlimals,devilon");
            add("age", "do-1-roku,vid-1-do-3-rokiv");
        }};
    }

    @Test
    public void getFilteredProducts_ReturnsOkStatusAndFilteredProductsPageDto() throws Exception {
        requestProductFilteringParams.add("page", "0");
        requestProductFilteringParams.add("size", "10");
        requestProductFilteringParams.add("price-sort", "asc");

        List<FilteredProductDto> expectedFilteredProductsPageDtoContent = new ArrayList<>() {{
            add(new FilteredProductDto("Лялька Русалочка", "lyalka-rusalochka", BigDecimal.valueOf(550),
                    BigInteger.valueOf(0), null));
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
        List<CategoryDto> expectedBrandCategories = new ArrayList<>() {{
            add(new CategoryDto("Devilon", "devilon"));
            add(new CategoryDto("Сurlimals", "сurlimals"));
        }};
        List<CategoryDto> expectedOriginCategories = new ArrayList<>() {{
            add(new CategoryDto("Китай", "china"));
            add(new CategoryDto("Україна", "ukraine"));
        }};
        List<CategoryDto> expectedAgeCategories = new ArrayList<>() {{
            add(new CategoryDto("від 1 до 3 років", "vid-1-do-3-rokiv"));
            add(new CategoryDto("від 6 до 9 років", "vid-6-do-9-rokiv"));
        }};
        var expectedFilteringProductCategoriesDto = FilteringCategoriesDto
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

    @Test
    public void getFilteringProductCategories_ReturnsOkStatusAndFilteringProductCategoriesDto() throws Exception {
        List<CategoryDto> expectedBrandCategories = new ArrayList<>() {{
            add(new CategoryDto("Devilon", "devilon"));
            add(new CategoryDto("Disney", "disney"));
            add(new CategoryDto("CoComelon", "сoсomelon"));
            add(new CategoryDto("Сurlimals", "сurlimals"));
        }};
        List<CategoryDto> expectedOriginCategories = new ArrayList<>() {{
            add(new CategoryDto("Китай", "china"));
            add(new CategoryDto("Україна", "ukraine"));
        }};
        List<CategoryDto> expectedAgeCategories = new ArrayList<>() {{
            add(new CategoryDto("від 1 до 3 років", "vid-1-do-3-rokiv"));
            add(new CategoryDto("від 6 до 9 років", "vid-6-do-9-rokiv"));
        }};
        var expectedFilteringProductCategoriesDto = FilteringCategoriesDto
                .builder()
                .originCategories(expectedOriginCategories)
                .brandCategories(expectedBrandCategories)
                .ageCategories(expectedAgeCategories)
                .build();

        ResultActions response = mockMvc.perform(get("/api/v1/products/categories")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedFilteringProductCategoriesDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void getProductBySlug_ExistentProductSlug_ReturnsOkStatusAndProductDto() throws Exception {
        String existentProductSlug = "lyalka-darynka";

        String expectedProductDescription = "Ця іграшка обєднує інноваційний дизайн та розвиваючий функціонал, що " +
                "сприяє розвитку навичок у дітей. Вона створює захоплюючий світ уяви, розвиваючи логічне мислення та " +
                "творчість. Іграшка безпечна, енергоефективна і сприяє розвитку спостережливості, уваги та " +
                "винахідливості у дітей, забезпечуючи незабутні враження та навчальний досвід.";
        List<CategoryDto> expectedProductAgeCategories = new ArrayList<>() {{
            add(new CategoryDto("від 1 до 3 років", "vid-1-do-3-rokiv"));
            add(new CategoryDto("від 6 до 9 років", "vid-6-do-9-rokiv"));
        }};
        ProductDto expectedProductDto = ProductDto
                .builder()
                .name("Лялька Даринка")
                .slug("lyalka-darynka")
                .description(expectedProductDescription)
                .price(BigDecimal.valueOf(900))
                .availableQuantity(BigInteger.valueOf(200))
                .images(new ArrayList<>())
                .originCategory(new CategoryDto("Україна", "ukraine"))
                .brandCategory(new CategoryDto("Сurlimals", "сurlimals"))
                .ageCategories(expectedProductAgeCategories)
                .build();

        ResultActions response = mockMvc.perform(get("/api/v1/products/{productSlug}", existentProductSlug)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedProductDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void getProductBySlug_NonExistentProductSlug_ReturnsNotFoundStatusAndResponseStatusExceptionDto() throws Exception {
        String nonExistentProductSlug = "non-existent-product-slug";
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        String productNotFoundExceptionMg = String.format("The product with slug: %s is not found.", nonExistentProductSlug);
        var responseStatusExceptionDto = new ExceptionDto(httpStatus.value(), httpStatus.name(),
                productNotFoundExceptionMg);


        ResultActions response = mockMvc.perform(get("/api/v1/products/{productSlug}", nonExistentProductSlug)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());
    }
}
