package com.kopchak.worldoftoys.dto.product.category;

import lombok.Builder;

@Builder
public record CategoryDto(String name, String slug) {
}
