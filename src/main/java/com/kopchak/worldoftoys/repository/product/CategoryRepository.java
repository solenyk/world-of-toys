package com.kopchak.worldoftoys.repository.product;

import com.kopchak.worldoftoys.domain.product.Product;
import com.kopchak.worldoftoys.domain.product.category.ProductCategory;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CategoryRepository {
    <T extends ProductCategory> Optional<T> findById(Integer id, Class<T> productCategoryType);

    <T extends ProductCategory> Set<T> findAll(Class<T> productCategoryType);

    <T extends ProductCategory> boolean isCategoryWithNameExists(Class<T> productCategoryType, String name);

    <T extends ProductCategory> void deleteByIdAndType(Class<T> productCategoryType, Integer id);

    <T extends ProductCategory> void updateNameByIdAndType(Class<T> categoryType, Integer id, String name);

    <T extends ProductCategory> void create(Class<T> categoryType, String name) throws ReflectiveOperationException;

    List<ProductCategory> findUniqueBrandCategoryList(Specification<Product> spec);

    List<ProductCategory> findUniqueOriginCategoryList(Specification<Product> spec);

    List<ProductCategory> findUniqueAgeCategoryList(Specification<Product> spec);

    <T extends ProductCategory> boolean containsProductsInCategory(Class<T> categoryType, Integer id);
}
