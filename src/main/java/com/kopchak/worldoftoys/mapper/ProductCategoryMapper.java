package com.kopchak.worldoftoys.mapper;

import com.kopchak.worldoftoys.dto.product.category.ProductCategoryDto;
import com.kopchak.worldoftoys.model.product.category.ProductCategory;

public class ProductCategoryMapper {
    public ProductCategoryDto toProductCategoryDto(ProductCategory productCategory) {
        return ProductCategoryDto
                .builder()
                .name(productCategory.getName())
                .slug(productCategory.getSlug())
                .build();
    }
}
