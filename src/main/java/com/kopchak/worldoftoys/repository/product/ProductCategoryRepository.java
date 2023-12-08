package com.kopchak.worldoftoys.repository.product;

import com.kopchak.worldoftoys.dto.product.category.FilteringProductCategoriesDto;
import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.model.product.category.ProductCategory;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

public interface ProductCategoryRepository {
    FilteringProductCategoriesDto findUniqueFilteringProductCategories(Specification<Product> spec);

    Optional<ProductCategory> findById(Integer id, Class<? extends ProductCategory> productCategorySubType);
}
