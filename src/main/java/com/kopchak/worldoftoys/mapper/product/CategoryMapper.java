package com.kopchak.worldoftoys.mapper.product;

import com.kopchak.worldoftoys.domain.product.category.ProductCategory;
import com.kopchak.worldoftoys.dto.admin.category.AdminCategoryDto;
import com.kopchak.worldoftoys.dto.product.category.CategoryDto;
import com.kopchak.worldoftoys.dto.product.category.FilteringCategoriesDto;
import org.mapstruct.Mapper;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class CategoryMapper {
    public FilteringCategoriesDto toFilteringCategoriesDto(List<ProductCategory> brandCategories,
                                                           List<ProductCategory> originCategories,
                                                           List<ProductCategory> ageCategories) {
        return FilteringCategoriesDto
                .builder()
                .brandCategories(toCategoryDtoList(brandCategories))
                .originCategories(toCategoryDtoList(originCategories))
                .ageCategories(toCategoryDtoList(ageCategories))
                .build();
    }

    public Set<AdminCategoryDto> toAdminCategoryDtoSet(Set<? extends ProductCategory> productCategories) {
        return productCategories.stream().map(this::toAdminCategoryDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    protected abstract List<CategoryDto> toCategoryDtoList(List<ProductCategory> productCategories);

    protected <T extends ProductCategory> AdminCategoryDto toAdminCategoryDto(T productCategory) {
        return new AdminCategoryDto(productCategory.getId(), productCategory.getName());
    }
}
