package com.kopchak.worldoftoys.mapper;

import com.kopchak.worldoftoys.dto.product.ProductDto;
import com.kopchak.worldoftoys.model.product.Product;

import java.util.stream.Collectors;

public class ProductMapper {
    public ProductDto toProductDto(Product product) {
        ProductCategoryMapper productCategoryMapper = new ProductCategoryMapper();
        ImageMapper imageMapper = new ImageMapper();
        return ProductDto
                .builder()
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .price(product.getPrice())
                .availableQuantity(product.getAvailableQuantity())
                .images(product.getImages().stream().map(imageMapper::toImageDto).collect(Collectors.toList()))
                .originCategory(productCategoryMapper.toProductCategoryDto(product.getOriginCategory()))
                .brandCategory(productCategoryMapper.toProductCategoryDto(product.getBrandCategory()))
                .ageCategory(product.getAgeCategory().stream().map(productCategoryMapper::toProductCategoryDto).collect(Collectors.toList()))
                .build();
    }
}
