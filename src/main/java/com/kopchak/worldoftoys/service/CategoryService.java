package com.kopchak.worldoftoys.service;

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
    Set<AdminCategoryDto> getAdminCategories(String categoryType)
            throws CategoryNotFoundException, InvalidCategoryTypeException;

    void deleteCategory(String category, Integer categoryId)
            throws CategoryContainsProductsException, InvalidCategoryTypeException;

    void updateCategory(String categoryType, Integer categoryId, CategoryNameDto categoryNameDto)
            throws CategoryNotFoundException, CategoryAlreadyExistsException, InvalidCategoryTypeException;

    void createCategory(String categoryType, CategoryNameDto categoryNameDto)
            throws CategoryAlreadyExistsException, InvalidCategoryTypeException, CategoryCreationException;
}
