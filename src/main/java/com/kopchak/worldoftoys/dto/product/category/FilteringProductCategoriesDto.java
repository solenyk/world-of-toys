package com.kopchak.worldoftoys.dto.product.category;

import lombok.Builder;

import java.util.List;

@Builder
public record FilteringProductCategoriesDto(List<ProductCategoryDto> brandCategories,
                                            List<ProductCategoryDto> originCategories,
                                            List<ProductCategoryDto> ageCategories) {
}
