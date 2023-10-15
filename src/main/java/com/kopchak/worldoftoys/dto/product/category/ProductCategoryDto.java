package com.kopchak.worldoftoys.dto.product.category;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class ProductCategoryDto {
    private String name;
    private String slug;
}
