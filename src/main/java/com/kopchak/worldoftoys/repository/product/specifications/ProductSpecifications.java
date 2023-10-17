package com.kopchak.worldoftoys.repository.product.specifications;

import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.model.product.category.ProductCategory;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

public interface ProductSpecifications {
    Specification<Product> hasProductName(String name);
    Specification<Product> hasProductInOriginCategory(List<String> originCategories);
    Specification<Product> hasPriceLessThanOrEqualTo(BigDecimal maxPrice);

    Specification<Product> hasPriceGreaterThanOrEqualTo(BigDecimal minPrice);

    Specification<Product> hasProductInBrandCategory(List<String> brandCategories);

    Specification<Product> hasProductInAgeCategory(List<String> ageCategories);
}
