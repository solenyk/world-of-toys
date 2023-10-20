package com.kopchak.worldoftoys.mapper;

import com.kopchak.worldoftoys.dto.product.FilteredProductDto;
import com.kopchak.worldoftoys.dto.product.FilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.product.ProductDto;
import com.kopchak.worldoftoys.dto.product.category.ProductCategoryDto;
import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.model.product.category.ProductCategory;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        uses = {ImageMapper.class, ProductCategoryMapper.class})
public interface ProductMapper {
    FilteredProductDto toFilteredProductDto(Product product);

    ProductDto toProductDto(Product product);

    default FilteredProductsPageDto toFilteredProductsPageDto(Page<Product> productPage) {
        if(productPage == null){
            return null;
        }
        List<FilteredProductDto> filteredProductsDtoSet = productPage
                .getContent()
                .stream()
                .map(this::toFilteredProductDto)
                .collect(Collectors.toList());
        return FilteredProductsPageDto
                .builder()
                .content(filteredProductsDtoSet)
                .totalElementsAmount(productPage.getTotalElements())
                .totalPagesAmount(productPage.getTotalPages())
                .build();
    }
}
