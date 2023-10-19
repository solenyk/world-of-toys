package com.kopchak.worldoftoys.mapper;

import com.kopchak.worldoftoys.dto.product.category.ProductCategoryDto;
import com.kopchak.worldoftoys.model.product.category.ProductCategory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductCategoryMapper {
    ProductCategoryDto toProductCategoryDto(ProductCategory productCategory);
}
