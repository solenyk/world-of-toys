package com.kopchak.worldoftoys.dto.product;

import com.kopchak.worldoftoys.dto.image.ImageDto;
import com.kopchak.worldoftoys.dto.product.category.ProductCategoryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ProductDto {
    private String name;

    private String slug;

    private String description;

    private BigDecimal price;

    private BigInteger availableQuantity;

    private List<ImageDto> images;

    private ProductCategoryDto originCategory;

    private ProductCategoryDto brandCategory;

    private List<ProductCategoryDto> ageCategories;
}
