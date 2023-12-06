package com.kopchak.worldoftoys.dto.admin.product;

import java.util.List;

public record AdminFilteredProductsPageDto(List<AdminFilteredProductDto> content, long totalElementsAmount,
                                           long totalPagesAmount) {
}
