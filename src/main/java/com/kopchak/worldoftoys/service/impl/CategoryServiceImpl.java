package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.domain.product.Product;
import com.kopchak.worldoftoys.domain.product.category.AgeCategory;
import com.kopchak.worldoftoys.domain.product.category.BrandCategory;
import com.kopchak.worldoftoys.domain.product.category.OriginCategory;
import com.kopchak.worldoftoys.domain.product.category.ProductCategory;
import com.kopchak.worldoftoys.domain.product.category.type.CategoryType;
import com.kopchak.worldoftoys.dto.admin.category.AdminCategoryDto;
import com.kopchak.worldoftoys.dto.admin.category.CategoryNameDto;
import com.kopchak.worldoftoys.dto.product.category.FilteringCategoriesDto;
import com.kopchak.worldoftoys.exception.exception.category.*;
import com.kopchak.worldoftoys.mapper.product.CategoryMapper;
import com.kopchak.worldoftoys.repository.product.CategoryRepository;
import com.kopchak.worldoftoys.repository.specifications.impl.ProductSpecificationsImpl;
import com.kopchak.worldoftoys.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductSpecificationsImpl productSpecifications;
    private final CategoryMapper categoryMapper;

    @Override
    public FilteringCategoriesDto getFilteringCategories(String productName, BigDecimal minPrice, BigDecimal maxPrice,
                                                         List<String> originCategories, List<String> brandCategories,
                                                         List<String> ageCategories) {
        Specification<Product> spec = productSpecifications.filterByProductNamePriceAndCategories(productName, minPrice,
                maxPrice, originCategories, brandCategories, ageCategories, null);
        var filteringProductCategoriesDto = categoryMapper.toFilteringCategoriesDto(
                categoryRepository.findUniqueBrandCategoryList(spec),
                categoryRepository.findUniqueOriginCategoryList(spec),
                categoryRepository.findUniqueAgeCategoryList(spec)
        );
        log.info("Fetched filtering product categories - Product Name: '{}', Min Price: {}, Max Price: {}, " +
                        "Origin Categories: {}, Brand Categories: {}, Age Categories: {}",
                productName, minPrice, maxPrice, originCategories, brandCategories, ageCategories);
        return filteringProductCategoriesDto;
    }

    @Override
    public Set<AdminCategoryDto> getAdminCategories(String categoryType) throws InvalidCategoryTypeException {
        Class<? extends ProductCategory> categoryClass = getCategoryByCategoryType(categoryType);
        var categories = categoryRepository.findAllCategories(categoryClass);
        return categoryMapper.toAdminCategoryDtoSet(categories);
    }

    @Override
    public void deleteCategory(String categoryType, Integer categoryId)
            throws CategoryContainsProductsException, InvalidCategoryTypeException {
        Class<? extends ProductCategory> categoryClass = getCategoryByCategoryType(categoryType);
        categoryRepository.deleteCategory(categoryClass, categoryId);
    }

    @Override
    public void updateCategory(String categoryType, Integer categoryId, CategoryNameDto categoryNameDto)
            throws CategoryNotFoundException, CategoryAlreadyExistsException, InvalidCategoryTypeException {
        Class<? extends ProductCategory> categoryClass = getCategoryByCategoryType(categoryType);
        categoryRepository.updateCategory(categoryClass, categoryId, categoryNameDto.name());
    }

    @Override
    public void createCategory(String categoryType, CategoryNameDto categoryNameDto)
            throws CategoryAlreadyExistsException, InvalidCategoryTypeException, CategoryCreationException {
        Class<? extends ProductCategory> categoryClass = getCategoryByCategoryType(categoryType);
        categoryRepository.createCategory(categoryClass, categoryNameDto.name());
    }

    private Class<? extends ProductCategory> getCategoryByCategoryType(String categoryType)
            throws InvalidCategoryTypeException {
        return switch (CategoryType.findByValue(categoryType)) {
            case BRANDS -> BrandCategory.class;
            case ORIGINS -> OriginCategory.class;
            case AGES -> AgeCategory.class;
        };
    }
}
