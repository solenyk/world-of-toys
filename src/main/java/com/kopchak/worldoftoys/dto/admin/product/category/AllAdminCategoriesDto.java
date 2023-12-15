package com.kopchak.worldoftoys.dto.admin.product.category;

import java.util.Set;


public record AllAdminCategoriesDto(Set<AdminProductCategoryDto> brandCategories,
                                    Set<AdminProductCategoryDto> originCategories,
                                    Set<AdminProductCategoryDto> ageCategories) {
}
