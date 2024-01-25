package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.domain.product.Product;
import com.kopchak.worldoftoys.domain.product.category.ProductCategory;
import com.kopchak.worldoftoys.domain.product.category.type.CategoryType;
import com.kopchak.worldoftoys.dto.admin.category.AdminCategoryDto;
import com.kopchak.worldoftoys.dto.admin.category.CategoryNameDto;
import com.kopchak.worldoftoys.dto.product.category.FilteringCategoriesDto;
import com.kopchak.worldoftoys.exception.exception.category.CategoryContainsProductsException;
import com.kopchak.worldoftoys.exception.exception.category.CategoryCreationException;
import com.kopchak.worldoftoys.exception.exception.category.DuplicateCategoryNameException;
import com.kopchak.worldoftoys.mapper.product.CategoryMapper;
import com.kopchak.worldoftoys.repository.product.CategoryRepository;
import com.kopchak.worldoftoys.repository.specifications.impl.ProductSpecificationsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductSpecificationsImpl productSpecifications;
    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;
    private final static CategoryType categoryType = CategoryType.ORIGINS;
    private final static Integer categoryId = 1;
    private String productName;
    private BigDecimal minProductPrice;
    private BigDecimal maxProductPrice;
    private List<String> originCategories;
    private List<String> brandCategories;
    private List<String> ageCategories;
    private Specification<Product> spec;
    private CategoryNameDto categoryNameDto;

    @BeforeEach
    void setUp() {
        productName = "лялька";
        minProductPrice = BigDecimal.valueOf(350);
        maxProductPrice = BigDecimal.valueOf(1000);
        originCategories = List.of("china", "ukraine");
        brandCategories = List.of("сurlimals", "devilon");
        ageCategories = List.of("do-1-roku", "vid-1-do-3-rokiv");
        spec = Specification.where(null);
        categoryNameDto = new CategoryNameDto("category-name");
    }

    @Test
    public void getFilteringProductCategories_ReturnsFilteringProductCategoriesDto() {
        var productCategories = new ArrayList<ProductCategory>();
        var expectedFilteringProductCategoriesDto = FilteringCategoriesDto.builder().build();

        when(productSpecifications.filterByProductNamePriceAndCategories(eq(productName), eq(minProductPrice),
                eq(maxProductPrice), eq(originCategories), eq(brandCategories), eq(ageCategories), any()))
                .thenReturn(spec);
        when(categoryRepository.findUniqueBrandCategoryList(spec)).thenReturn(productCategories);
        when(categoryRepository.findUniqueOriginCategoryList(spec)).thenReturn(productCategories);
        when(categoryRepository.findUniqueAgeCategoryList(spec)).thenReturn(productCategories);
        when(categoryMapper.toFilteringCategoriesDto(eq(productCategories), eq(productCategories),
                eq(productCategories))).thenReturn(expectedFilteringProductCategoriesDto);

        var actualFilteringProductCategoriesDto = categoryService.getFilteringCategories(productName,
                minProductPrice, maxProductPrice, originCategories, brandCategories, ageCategories);

        assertThat(actualFilteringProductCategoriesDto).isNotNull();
        assertThat(actualFilteringProductCategoriesDto).isEqualTo(expectedFilteringProductCategoriesDto);
    }

    @Test
    public void getAdminCategories_ReturnsAdminCategoryDtoSet() {
        CategoryType categoryType = CategoryType.ORIGINS;
        AdminCategoryDto adminCategoryDto = new AdminCategoryDto(1, "name");
        Set<AdminCategoryDto> expectedAdminCategoryDtoSet = Set.of(adminCategoryDto);

        when(categoryRepository.findAll(eq(categoryType.getCategory()))).thenReturn(new HashSet<>());
        when(categoryMapper.toAdminCategoryDtoSet(anySet())).thenReturn(expectedAdminCategoryDtoSet);

        Set<AdminCategoryDto> actualAdminCategoryDtoSet = categoryService.getAdminCategories(categoryType);

        assertThat(actualAdminCategoryDtoSet).isNotNull();
        assertThat(actualAdminCategoryDtoSet.size()).isEqualTo(1);
        assertThat(actualAdminCategoryDtoSet.contains(adminCategoryDto)).isTrue();
    }

    @Test
    public void deleteCategory() throws CategoryContainsProductsException {
        doNothing().when(categoryRepository).deleteByIdAndType(eq(categoryId), eq(categoryType.getCategory()));

        categoryService.deleteCategory(categoryType, categoryId);

        verify(categoryRepository).deleteByIdAndType(eq(categoryId), eq(categoryType.getCategory()));
    }

    @Test
    public void updateCategory() throws DuplicateCategoryNameException {
        doNothing().when(categoryRepository)
                .updateNameByIdAndType(categoryId, categoryNameDto.name(), categoryType.getCategory());

        categoryService.updateCategory(categoryType, categoryId, categoryNameDto);

        verify(categoryRepository).updateNameByIdAndType(categoryId, categoryNameDto.name(), categoryType.getCategory());
    }

    @Test
    public void createCategory() throws DuplicateCategoryNameException, CategoryCreationException, ReflectiveOperationException {
        doNothing().when(categoryRepository).create(categoryNameDto.name(), categoryType.getCategory());

        categoryService.createCategory(categoryType, categoryNameDto);

        verify(categoryRepository).create(categoryNameDto.name(), categoryType.getCategory());
    }
}