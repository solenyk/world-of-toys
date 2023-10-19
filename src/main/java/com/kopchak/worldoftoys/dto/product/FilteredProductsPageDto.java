package com.kopchak.worldoftoys.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class FilteredProductsPageDto {
    List<FilteredProductDto> content;
    long totalElementsAmount;
    long totalPagesAmount;
}
