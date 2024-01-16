package com.kopchak.worldoftoys.repository.product;

import com.kopchak.worldoftoys.dto.product.category.FilteringCategoriesDto;
import com.kopchak.worldoftoys.exception.InvalidCategoryTypeException;
import com.kopchak.worldoftoys.domain.product.Product;
import com.kopchak.worldoftoys.domain.product.category.ProductCategory;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

public interface CategoryRepository {
    FilteringCategoriesDto findUniqueFilteringProductCategories(Specification<Product> spec);

    <T extends ProductCategory> T findById(Integer id, Class<T> productCategoryType) throws InvalidCategoryTypeException;

    <T extends ProductCategory> Set<T> findAllCategories(Class<T> productCategoryType);

    <T extends ProductCategory> void deleteCategory(Class<T> productCategoryType, Integer id)
            throws InvalidCategoryTypeException;

    <T extends ProductCategory> void updateCategory(Class<T> categoryType, Integer id, String name)
            throws InvalidCategoryTypeException;

    <T extends ProductCategory> void addCategory(Class<T> categoryType, String name) throws InvalidCategoryTypeException;
}
