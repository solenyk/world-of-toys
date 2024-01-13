package com.kopchak.worldoftoys.mapper.product;

import com.kopchak.worldoftoys.dto.admin.product.category.AdminCategoryDto;
import com.kopchak.worldoftoys.dto.product.category.CategoryDto;
import com.kopchak.worldoftoys.domain.product.category.ProductCategory;
import org.mapstruct.Mapper;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class CategoryMapper {
    public abstract List<CategoryDto> toCategoryDtoList(List<ProductCategory> productCategories);

    public Set<AdminCategoryDto> toAdminCategoryDtoSet(Set<? extends ProductCategory> productCategories) {
        return productCategories.stream().map(this::toAdminCategoryDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    protected abstract CategoryDto toCategoryDto(ProductCategory productCategory);

    protected <T extends ProductCategory> AdminCategoryDto toAdminCategoryDto(T productCategory) {
        return new AdminCategoryDto(productCategory.getId(), productCategory.getName());
    }
}
