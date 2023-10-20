package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.product.FilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.product.ProductDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductService {
    FilteredProductsPageDto getAllProducts(int page, int size, String productName, BigDecimal minPrice, BigDecimal maxPrice,
                                           List<String> originCategories, List<String> brandCategories,
                                           List<String> ageCategories, String priceSortOrder);

    Optional<ProductDto> getProductDtoBySlug(String slug);
}
