package com.kopchak.worldoftoys.repository.product;

import com.kopchak.worldoftoys.domain.product.Product;
import com.kopchak.worldoftoys.domain.product.category.ProductCategory;
import com.kopchak.worldoftoys.exception.CategoryContainsProductsException;
import com.kopchak.worldoftoys.exception.InvalidCategoryTypeException;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Set;

public interface CategoryRepository {
    <T extends ProductCategory> T findById(Integer id, Class<T> productCategoryType) throws InvalidCategoryTypeException;

    <T extends ProductCategory> Set<T> findAllCategories(Class<T> productCategoryType);

    <T extends ProductCategory> void deleteCategory(Class<T> productCategoryType, Integer id)
            throws InvalidCategoryTypeException, CategoryContainsProductsException;

    <T extends ProductCategory> void updateCategory(Class<T> categoryType, Integer id, String name)
            throws InvalidCategoryTypeException;

    <T extends ProductCategory> void addCategory(Class<T> categoryType, String name) throws InvalidCategoryTypeException;

    List<ProductCategory> findUniqueBrandCategoryList(Specification<Product> spec);

    List<ProductCategory> findUniqueOriginCategoryList(Specification<Product> spec);

    List<ProductCategory> findUniqueAgeCategoryList(Specification<Product> spec);
}
