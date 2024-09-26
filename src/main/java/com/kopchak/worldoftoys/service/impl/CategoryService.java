package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.domain.product.Product;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductSpecificationsImpl productSpecifications;
    private final CategoryMapper categoryMapper;

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

    public Set<AdminCategoryDto> getAdminCategories(CategoryType categoryType) {
        var categories = categoryRepository.findAll(categoryType.getCategory());
        return categoryMapper.toAdminCategoryDtoSet(categories);
    }

    public void deleteCategory(CategoryType categoryType, Integer categoryId) {
        if (categoryRepository.containsProductsInCategory(categoryId, categoryType.getCategory())) {
            throw new CategoryContainsProductsException(String.format("It is not possible to delete a category " +
                    "with id: %d because there are products in this category.", categoryId));
        }
        categoryRepository.deleteByIdAndType(categoryId, categoryType.getCategory());
    }

    public void updateCategory(CategoryType categoryType, Integer categoryId, CategoryNameDto categoryNameDto) {
        String categoryName = categoryNameDto.name();
        if (categoryRepository.isCategoryWithNameExists(categoryName, categoryType.getCategory())) {
            throw new DuplicateCategoryNameException(String.format("Category with name: %s already exist", categoryName));
        }
        categoryRepository.updateNameByIdAndType(categoryId, categoryName, categoryType.getCategory());
    }

    public void createCategory(CategoryType categoryType, CategoryNameDto categoryNameDto) {
        String categoryName = categoryNameDto.name();
        if (categoryRepository.isCategoryWithNameExists(categoryName, categoryType.getCategory())) {
            throw new DuplicateCategoryNameException(String.format("Category with name: %s already exist", categoryName));
        }
        try {
            categoryRepository.create(categoryName, categoryType.getCategory());
        } catch (ReflectiveOperationException e) {
            throw new CategoryCreationException(e.getMessage());
        }
    }

    public <T extends ProductCategory> T findCategoryByIdAndType(Integer id, Class<T> categoryType) {
        Optional<T> optionalCategory = categoryRepository.findByIdAndType(id, categoryType);
        if (optionalCategory.isPresent()) {
            return optionalCategory.get();
        }
        throw new CategoryNotFoundException(String.format("The category type: %s with id: %d is not found",
                categoryType.getSimpleName(), id));
    }
}
