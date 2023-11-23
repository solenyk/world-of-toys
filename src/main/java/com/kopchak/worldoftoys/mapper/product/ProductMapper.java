package com.kopchak.worldoftoys.mapper.product;

import com.kopchak.worldoftoys.dto.product.FilteredProductDto;
import com.kopchak.worldoftoys.dto.product.FilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.product.ProductDto;
import com.kopchak.worldoftoys.model.product.Product;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ImageMapper.class, ProductCategoryMapper.class})
public interface ProductMapper {
    ProductDto toProductDto(Product product);
    List<FilteredProductDto> toFilteredProductDtoList(List<Product> products);
    default FilteredProductsPageDto toFilteredProductsPageDto(Page<Product> productPage){
        return new FilteredProductsPageDto(toFilteredProductDtoList(productPage.getContent()),
                productPage.getTotalElements(), productPage.getTotalPages());
    }
}
