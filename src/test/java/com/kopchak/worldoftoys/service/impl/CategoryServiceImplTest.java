package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.domain.product.Product;
import com.kopchak.worldoftoys.domain.product.category.OriginCategory;
import com.kopchak.worldoftoys.domain.product.category.ProductCategory;
import com.kopchak.worldoftoys.domain.product.category.type.CategoryType;
import com.kopchak.worldoftoys.dto.admin.category.AdminCategoryDto;
import com.kopchak.worldoftoys.dto.admin.category.CategoryNameDto;
import com.kopchak.worldoftoys.dto.product.category.FilteringCategoriesDto;
import com.kopchak.worldoftoys.exception.exception.category.CategoryContainsProductsException;
import com.kopchak.worldoftoys.exception.exception.category.CategoryCreationException;
import com.kopchak.worldoftoys.exception.exception.category.CategoryNotFoundException;
import com.kopchak.worldoftoys.exception.exception.category.DuplicateCategoryNameException;
import com.kopchak.worldoftoys.mapper.product.CategoryMapper;
import com.kopchak.worldoftoys.repository.product.CategoryRepository;
import com.kopchak.worldoftoys.repository.specifications.impl.ProductSpecificationsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private final static CategoryType CATEGORY_TYPE = CategoryType.ORIGINS;
    private final static Integer CATEGORY_ID = 1;
    private final static String CATEGORY_NAME = "category-name";
    private final static String DUPLICATE_CATEGORY_NAME_EXCEPTION_MSG =
            String.format("Category with name: %s already exist", CATEGORY_NAME);
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
        categoryNameDto = new CategoryNameDto(CATEGORY_NAME);
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
        AdminCategoryDto adminCategoryDto = new AdminCategoryDto(1, "name");
        Set<AdminCategoryDto> expectedAdminCategoryDtoSet = Set.of(adminCategoryDto);

        when(categoryRepository.findAll(eq(CATEGORY_TYPE.getCategory()))).thenReturn(new HashSet<>());
        when(categoryMapper.toAdminCategoryDtoSet(anySet())).thenReturn(expectedAdminCategoryDtoSet);

        Set<AdminCategoryDto> actualAdminCategoryDtoSet = categoryService.getAdminCategories(CATEGORY_TYPE);

        assertThat(actualAdminCategoryDtoSet).isNotNull();
        assertThat(actualAdminCategoryDtoSet.size()).isEqualTo(1);
        assertThat(actualAdminCategoryDtoSet.contains(adminCategoryDto)).isTrue();
    }

    @Test
    public void deleteCategory_CategoryWithoutProducts() throws CategoryContainsProductsException {
        when(categoryRepository.containsProductsInCategory(CATEGORY_ID, CATEGORY_TYPE.getCategory())).thenReturn(false);
        doNothing().when(categoryRepository).deleteByIdAndType(eq(CATEGORY_ID), eq(CATEGORY_TYPE.getCategory()));

        categoryService.deleteCategory(CATEGORY_TYPE, CATEGORY_ID);

        verify(categoryRepository).deleteByIdAndType(eq(CATEGORY_ID), eq(CATEGORY_TYPE.getCategory()));
    }

    @Test
    public void deleteCategory_CategoryWithProducts_ThrowsCategoryContainsProductsException() {
        String categoryContainsProductsExceptionMsg = String.format("It is not possible to delete a category " +
                "with id: %d because there are products in this category.", CATEGORY_ID);

        when(categoryRepository.containsProductsInCategory(eq(CATEGORY_ID), eq(CATEGORY_TYPE.getCategory())))
                .thenReturn(true);

        assertException(CategoryContainsProductsException.class, categoryContainsProductsExceptionMsg,
                () -> categoryService.deleteCategory(CATEGORY_TYPE, CATEGORY_ID));

        verify(categoryRepository, never()).deleteByIdAndType(any(), any());
    }

    @Test
    public void updateCategory_NonExistentCategoryName() throws DuplicateCategoryNameException {
        when(categoryRepository.isCategoryWithNameExists(eq(CATEGORY_NAME), eq(CATEGORY_TYPE.getCategory())))
                .thenReturn(false);
        doNothing().when(categoryRepository)
                .updateNameByIdAndType(CATEGORY_ID, categoryNameDto.name(), CATEGORY_TYPE.getCategory());

        categoryService.updateCategory(CATEGORY_TYPE, CATEGORY_ID, categoryNameDto);

        verify(categoryRepository).updateNameByIdAndType(eq(CATEGORY_ID), eq(CATEGORY_NAME),
                eq(CATEGORY_TYPE.getCategory()));
    }

    @Test
    public void updateCategory_ExistentCategoryName_ThrowsDuplicateCategoryNameException() {
        when(categoryRepository.isCategoryWithNameExists(eq(CATEGORY_NAME), eq(CATEGORY_TYPE.getCategory())))
                .thenReturn(true);

        assertException(DuplicateCategoryNameException.class, DUPLICATE_CATEGORY_NAME_EXCEPTION_MSG,
                () -> categoryService.updateCategory(CATEGORY_TYPE, CATEGORY_ID, categoryNameDto));

        verify(categoryRepository, never()).updateNameByIdAndType(any(), any(), any());
    }

    @Test
    public void createCategory_NonExistentCategoryName() throws Exception {
        when(categoryRepository.isCategoryWithNameExists(eq(CATEGORY_NAME), eq(CATEGORY_TYPE.getCategory())))
                .thenReturn(false);
        doNothing().when(categoryRepository).create(eq(CATEGORY_NAME), eq(CATEGORY_TYPE.getCategory()));

        categoryService.createCategory(CATEGORY_TYPE, categoryNameDto);

        verify(categoryRepository).create(eq(CATEGORY_NAME), eq(CATEGORY_TYPE.getCategory()));
    }

    @Test
    public void createCategory_ExistentCategoryName_ThrowsDuplicateCategoryNameException() throws ReflectiveOperationException {
        when(categoryRepository.isCategoryWithNameExists(eq(CATEGORY_NAME), eq(CATEGORY_TYPE.getCategory())))
                .thenReturn(true);

        assertException(DuplicateCategoryNameException.class, DUPLICATE_CATEGORY_NAME_EXCEPTION_MSG,
                () -> categoryService.createCategory(CATEGORY_TYPE, categoryNameDto));

        verify(categoryRepository, never()).create(any(), any());
    }

    @Test
    public void createCategory_ThrowsReflectiveOperationException() throws Exception {
        String reflectiveOperationExceptionMsg = "Constructor without arguments does not exist";
        doThrow(new ReflectiveOperationException(reflectiveOperationExceptionMsg))
                .when(categoryRepository).create(eq(CATEGORY_NAME), eq(CATEGORY_TYPE.getCategory()));

        assertException(CategoryCreationException.class, reflectiveOperationExceptionMsg,
                () -> categoryService.createCategory(CATEGORY_TYPE, categoryNameDto));
    }

    @Test
    public void findCategoryByIdAndType_ExistentOriginCategory_ReturnsOriginCategory() throws CategoryNotFoundException {
        OriginCategory expectedOriginCategory = new OriginCategory();

        when(categoryRepository.findByIdAndType(eq(CATEGORY_ID), eq(OriginCategory.class)))
                .thenReturn(Optional.of(expectedOriginCategory));

        var actualOriginCategory = categoryService.findCategoryByIdAndType(CATEGORY_ID, CATEGORY_TYPE.getCategory());

        assertThat(actualOriginCategory).isNotNull();
        assertThat(actualOriginCategory).isEqualTo(expectedOriginCategory);
    }

    @Test
    public void findCategoryByIdAndType_NonExistentOriginCategory_ThrowsCategoryNotFoundException() {
        String categoryNotFoundExceptionMsg = String.format("The category type: %s with id: %d is not found",
                OriginCategory.class.getSimpleName(), CATEGORY_ID);
        when(categoryRepository.findByIdAndType(eq(CATEGORY_ID), eq(OriginCategory.class)))
                .thenReturn(Optional.empty());

        assertException(CategoryNotFoundException.class, categoryNotFoundExceptionMsg,
                () -> categoryService.findCategoryByIdAndType(CATEGORY_ID, CATEGORY_TYPE.getCategory()));
    }

    private void assertException(Class<? extends Exception> expectedExceptionType, String expectedMessage,
                                 Executable executable) {
        Exception exception = assertThrows(expectedExceptionType, executable);
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }
}