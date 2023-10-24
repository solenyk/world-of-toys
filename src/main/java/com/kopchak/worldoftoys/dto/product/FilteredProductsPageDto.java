package com.kopchak.worldoftoys.dto.product;

import java.util.List;

public record FilteredProductsPageDto(List<FilteredProductDto> content, long totalElementsAmount,
                                      long totalPagesAmount) {
}
