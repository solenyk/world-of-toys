package com.kopchak.worldoftoys.repository.specifications;

import com.kopchak.worldoftoys.domain.product.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

public interface ProductSpecifications {
    Specification<Product> filterByProductNamePriceAndCategories(String productName, BigDecimal minPrice,
                                                                 BigDecimal maxPrice, List<String> originCategories,
                                                                 List<String> brandCategories,
                                                                 List<String> ageCategories, String availability);

    Specification<Product> filterByAllCriteria(String productName, BigDecimal minPrice, BigDecimal maxPrice,
                                               List<String> originCategories, List<String> brandCategories,
                                               List<String> ageCategories, String priceSortOrder, String availability);
}
