package com.kopchak.worldoftoys.dto.product.category;

import lombok.Builder;

@Builder
public record ProductCategoryDto(String name, String slug) {
}
