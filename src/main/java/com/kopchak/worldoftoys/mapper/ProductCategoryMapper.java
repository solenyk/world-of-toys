package com.kopchak.worldoftoys.mapper;

import com.kopchak.worldoftoys.dto.product.category.ProductCategoryDto;
import com.kopchak.worldoftoys.model.product.category.ProductCategory;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductCategoryMapper {
    ProductCategoryDto toProductCategoryDto(ProductCategory productCategory);

    List<ProductCategoryDto> toProductCategoryDtoList(List<ProductCategory> productCategories);
}
