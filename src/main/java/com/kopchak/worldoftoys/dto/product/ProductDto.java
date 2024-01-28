package com.kopchak.worldoftoys.dto.product;

import com.kopchak.worldoftoys.dto.image.ImageDto;
import com.kopchak.worldoftoys.dto.product.category.CategoryDto;
import lombok.Builder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Builder
public record ProductDto(String name, String slug, String description, BigDecimal price, BigInteger availableQuantity,
                         ImageDto mainImage, List<ImageDto> images, CategoryDto originCategory,
                         CategoryDto brandCategory, List<CategoryDto> ageCategories) {
}
