package com.kopchak.worldoftoys.repository.product;

import com.kopchak.worldoftoys.domain.product.Product;
import com.kopchak.worldoftoys.domain.product.category.ProductCategory;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CategoryRepository {
    <T extends ProductCategory> Optional<T> findByIdAndType(Integer id, Class<T> categoryType);

    <T extends ProductCategory> Set<T> findAll(Class<T> productCategoryType);

    <T extends ProductCategory> void deleteByIdAndType(Integer id, Class<T> categoryType);

    <T extends ProductCategory> void updateNameByIdAndType(Integer id, String name, Class<T> categoryType);

    <T extends ProductCategory> void create(String name, Class<T> categoryType) throws ReflectiveOperationException;

    List<ProductCategory> findUniqueBrandCategoryList(Specification<Product> spec);

    List<ProductCategory> findUniqueOriginCategoryList(Specification<Product> spec);

    List<ProductCategory> findUniqueAgeCategoryList(Specification<Product> spec);

    <T extends ProductCategory> boolean containsProductsInCategory(Integer id, Class<T> categoryType);

    <T extends ProductCategory> boolean isCategoryWithNameExists(String name, Class<T> categoryType);
}
