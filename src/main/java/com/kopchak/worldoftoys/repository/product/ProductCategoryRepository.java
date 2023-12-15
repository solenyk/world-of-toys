package com.kopchak.worldoftoys.repository.product;

import com.kopchak.worldoftoys.dto.product.category.FilteringProductCategoriesDto;
import com.kopchak.worldoftoys.exception.exception.CategoryException;
import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.model.product.category.ProductCategory;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

public interface ProductCategoryRepository {
    FilteringProductCategoriesDto findUniqueFilteringProductCategories(Specification<Product> spec);

    <T extends ProductCategory> T findById(Integer id, Class<T> productCategoryType) throws CategoryException;

    <T extends ProductCategory> Set<T> findAllCategories(Class<T> productCategoryType);

    <T extends ProductCategory> void deleteCategory(Class<T> productCategoryType, Integer id)
            throws CategoryException;
}
