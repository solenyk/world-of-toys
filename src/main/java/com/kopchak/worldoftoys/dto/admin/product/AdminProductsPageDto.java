package com.kopchak.worldoftoys.dto.admin.product;

import lombok.Builder;

import java.util.List;

@Builder
public record AdminProductsPageDto(List<AdminFilteredProductDto> content, long totalElementsAmount,
                                   long totalPagesAmount) {
}
