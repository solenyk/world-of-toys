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
import com.kopchak.worldoftoys.service.CategoryService;
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
    public Set<AdminCategoryDto> getAdminCategories(CategoryType categoryType) {
        var categories = categoryRepository.findAll(categoryType.getCategory());
        return categoryMapper.toAdminCategoryDtoSet(categories);
    }

    @Override
    public void deleteCategory(CategoryType categoryType, Integer categoryId) throws CategoryContainsProductsException {
        if (categoryRepository.containsProductsInCategory(categoryId, categoryType.getCategory())) {
            throw new CategoryContainsProductsException(String.format("It is not possible to delete a category " +
                    "with id: %d because there are products in this category.", categoryId));
        }
        categoryRepository.deleteByIdAndType(categoryId, categoryType.getCategory());
    }

    @Override
    public void updateCategory(CategoryType categoryType, Integer categoryId, CategoryNameDto categoryNameDto)
            throws DuplicateCategoryNameException {
        String categoryName = categoryNameDto.name();
        if (categoryRepository.isCategoryWithNameExists(categoryName, categoryType.getCategory())) {
            throw new DuplicateCategoryNameException(String.format("Category with name: %s already exist", categoryName));
        }
        categoryRepository.updateNameByIdAndType(categoryId, categoryName, categoryType.getCategory());
    }

    @Override
    public void createCategory(CategoryType categoryType, CategoryNameDto categoryNameDto)
            throws DuplicateCategoryNameException, CategoryCreationException {
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

    @Override
    public <T extends ProductCategory> T findCategoryById(Integer id, Class<T> categoryType) throws CategoryNotFoundException {
        Optional<T> optionalCategory = categoryRepository.findByIdAndType(id, categoryType);
        if (optionalCategory.isPresent()) {
            return optionalCategory.get();
        }
        throw new CategoryNotFoundException(String.format("The category with id: %d is not found", id));
    }
}
