package com.kopchak.worldoftoys.mapper.product;

import com.kopchak.worldoftoys.dto.admin.product.category.AdminProductCategoryDto;
import com.kopchak.worldoftoys.dto.admin.product.category.AllAdminCategoriesDto;
import com.kopchak.worldoftoys.dto.product.category.ProductCategoryDto;
import com.kopchak.worldoftoys.model.product.category.ProductCategory;
import org.mapstruct.Mapper;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class ProductCategoryMapper {
    public abstract List<ProductCategoryDto> toProductCategoryDtoList(List<ProductCategory> productCategories);

    public AllAdminCategoriesDto toAllAdminCategoriesDto(Set<? extends ProductCategory> brandCategories,
                                                         Set<? extends ProductCategory> originCategories,
                                                         Set<? extends ProductCategory> ageCategories) {
        return new AllAdminCategoriesDto(toAdminProductCategoryDtoSet(brandCategories),
                toAdminProductCategoryDtoSet(originCategories), toAdminProductCategoryDtoSet(ageCategories));
    }

    protected abstract ProductCategoryDto toProductCategoryDto(ProductCategory productCategory);

    protected <T extends ProductCategory> AdminProductCategoryDto toAdminProductCategoryDto(T productCategory) {
        return new AdminProductCategoryDto(productCategory.getId(), productCategory.getName());
    }

    protected Set<AdminProductCategoryDto> toAdminProductCategoryDtoSet(Set<? extends ProductCategory> productCategories) {
        return productCategories.stream().map(this::toAdminProductCategoryDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
