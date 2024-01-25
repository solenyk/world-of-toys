package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.domain.product.category.ProductCategory;
import com.kopchak.worldoftoys.domain.product.category.type.CategoryType;
import com.kopchak.worldoftoys.dto.admin.category.AdminCategoryDto;
import com.kopchak.worldoftoys.dto.admin.category.CategoryNameDto;
import com.kopchak.worldoftoys.dto.product.category.FilteringCategoriesDto;
import com.kopchak.worldoftoys.exception.exception.category.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface CategoryService {
    FilteringCategoriesDto getFilteringCategories(String productName, BigDecimal minPrice, BigDecimal maxPrice,
                                                  List<String> originCategories, List<String> brandCategories,
                                                  List<String> ageCategories);

    Set<AdminCategoryDto> getAdminCategories(CategoryType categoryType);

    void deleteCategory(CategoryType categoryType, Integer categoryId) throws CategoryContainsProductsException;

    void updateCategory(CategoryType categoryType, Integer categoryId, CategoryNameDto categoryNameDto)
            throws DuplicateCategoryNameException;

    void createCategory(CategoryType categoryType, CategoryNameDto categoryNameDto)
            throws DuplicateCategoryNameException, CategoryCreationException;

    <T extends ProductCategory> T findCategoryById(Integer id, Class<T> categoryType) throws CategoryNotFoundException;
}
