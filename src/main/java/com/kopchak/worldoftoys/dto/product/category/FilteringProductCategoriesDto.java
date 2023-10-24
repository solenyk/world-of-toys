package com.kopchak.worldoftoys.dto.product.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class FilteringProductCategoriesDto {
    List<ProductCategoryDto> brandCategories;
    List<ProductCategoryDto> originCategories;
    List<ProductCategoryDto> ageCategories;
}
