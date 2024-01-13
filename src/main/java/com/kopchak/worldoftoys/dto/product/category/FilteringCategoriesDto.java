package com.kopchak.worldoftoys.dto.product.category;

import lombok.Builder;

import java.util.List;

@Builder
public record FilteringCategoriesDto(List<CategoryDto> brandCategories,
                                     List<CategoryDto> originCategories,
                                     List<CategoryDto> ageCategories) {
}
