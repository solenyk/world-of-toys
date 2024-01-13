package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.product.FilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.product.ProductDto;
import com.kopchak.worldoftoys.dto.product.category.FilteringCategoriesDto;
import com.kopchak.worldoftoys.mapper.product.ProductMapper;
import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.repository.product.CategoryRepository;
import com.kopchak.worldoftoys.repository.product.ProductRepository;
import com.kopchak.worldoftoys.repository.specifications.impl.ProductSpecificationsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductSpecificationsImpl productSpecifications;
    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;
    private String productName;
    private BigDecimal minProductPrice;
    private BigDecimal maxProductPrice;
    private List<String> originCategories;
    private List<String> brandCategories;
    private List<String> ageCategories;
    private Specification<Product> spec;

    @BeforeEach
    void setUp() {
        productName = "лялька";
        minProductPrice = BigDecimal.valueOf(350);
        maxProductPrice = BigDecimal.valueOf(1000);
        originCategories = List.of("china", "ukraine");
        brandCategories = List.of("сurlimals", "devilon");
        ageCategories = List.of("do-1-roku", "vid-1-do-3-rokiv");
        spec = Specification.where(null);
    }

    @Test
    public void getFilteredProducts_FilteringCriteria_ReturnsFilteredProductsPageDto() {
        String priceSortOrder = "asc";

        int page = 0;
        int size = 3;

        long expectedTotalElementsAmount = 3;
        long expectedTotalPagesAmount = 7;

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = new PageImpl<>(new ArrayList<>(), pageable, 20);
        FilteredProductsPageDto expectedFilteredProductsPageDto = new FilteredProductsPageDto(new ArrayList<>(),
                expectedTotalElementsAmount, expectedTotalPagesAmount);

        when(productSpecifications.filterByAllCriteria(eq(productName), eq(minProductPrice), eq(maxProductPrice),
                eq(originCategories), eq(brandCategories), eq(ageCategories), eq(priceSortOrder))).thenReturn(spec);
        when(productRepository.findAll(eq(spec), eq(pageable))).thenReturn(productPage);
        when(productMapper.toFilteredProductsPageDto(eq(productPage))).thenReturn(expectedFilteredProductsPageDto);

        FilteredProductsPageDto actualFilteredProductsPageDto = productService.getFilteredProducts(page, size,
                productName, minProductPrice, maxProductPrice, originCategories, brandCategories, ageCategories,
                priceSortOrder);

        assertThat(actualFilteredProductsPageDto).isNotNull();
        assertThat(actualFilteredProductsPageDto.totalElementsAmount()).isEqualTo(expectedTotalElementsAmount);
        assertThat(actualFilteredProductsPageDto.totalPagesAmount()).isEqualTo(expectedTotalPagesAmount);
        assertThat(actualFilteredProductsPageDto).isEqualTo(expectedFilteredProductsPageDto);
    }

    @Test
    public void getProductDtoBySlug_ProductSlug_ReturnsOptionalOfProductDto() {
        String productSlug = "lyalka-klaymber";
        Product product = new Product();
        ProductDto expectedProductDto = ProductDto.builder().build();

        when(productRepository.findBySlug(eq(productSlug))).thenReturn(Optional.of(product));
        when(productMapper.toProductDto(eq(product))).thenReturn(expectedProductDto);

        Optional<ProductDto> actualOptionalProductDto = productService.getProductDtoBySlug(productSlug);

        assertThat(actualOptionalProductDto).isNotNull();
        assertThat(actualOptionalProductDto).isPresent();
        assertThat(actualOptionalProductDto).isEqualTo(Optional.of(expectedProductDto));
    }


    @Test
    public void getFilteringProductCategories_FilteringCriteria_ReturnsFilteringProductCategoriesDto() {
        var expectedFilteringProductCategoriesDto = FilteringCategoriesDto.builder().build();

        when(productSpecifications.filterByProductNamePriceAndCategories(eq(productName), eq(minProductPrice),
                eq(maxProductPrice), eq(originCategories), eq(brandCategories), eq(ageCategories))).thenReturn(spec);
        when(categoryRepository.findUniqueFilteringProductCategories(eq(spec)))
                .thenReturn(expectedFilteringProductCategoriesDto);

        var actualFilteringProductCategoriesDto = productService.getFilteringCategories(productName,
                minProductPrice, maxProductPrice, originCategories, brandCategories, ageCategories);

        assertThat(actualFilteringProductCategoriesDto).isNotNull();
        assertThat(actualFilteringProductCategoriesDto).isEqualTo(expectedFilteringProductCategoriesDto);
    }
}