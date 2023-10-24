package com.kopchak.worldoftoys.repository.product;

import com.kopchak.worldoftoys.dto.product.category.FilteringProductCategoriesDto;
import com.kopchak.worldoftoys.model.product.Product;
import org.springframework.data.jpa.domain.Specification;

public interface ProductCategoryRepository {
    FilteringProductCategoriesDto findUniqueCategories(Specification<Product> spec);
}
