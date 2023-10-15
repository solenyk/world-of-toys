package com.kopchak.worldoftoys.dto.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class StoreProductsDto {
    List<ProductDto> content;
    long totalElementsAmount;
    long totalPagesAmount;
}
