package com.kopchak.worldoftoys.dto.product;

import com.kopchak.worldoftoys.dto.image.ImageDto;
import com.kopchak.worldoftoys.dto.product.category.ProductCategoryDto;
import lombok.Builder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Builder
public record ProductDto(String name, String slug, String description, BigDecimal price, BigInteger availableQuantity,
                         List<ImageDto> images, ProductCategoryDto originCategory, ProductCategoryDto brandCategory,
                         List<ProductCategoryDto> ageCategories) {
}
