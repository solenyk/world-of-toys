package com.kopchak.worldoftoys.repository.product;

import com.kopchak.worldoftoys.dto.product.category.FilteringProductCategoriesDto;
import com.kopchak.worldoftoys.exception.exception.CategoryNotFoundException;
import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.model.product.category.ProductCategory;
import org.springframework.data.jpa.domain.Specification;

public interface ProductCategoryRepository {
    FilteringProductCategoriesDto findUniqueFilteringProductCategories(Specification<Product> spec);
    <T extends ProductCategory> T findById(Integer id, Class<T> productCategoryType) throws CategoryNotFoundException;
}
