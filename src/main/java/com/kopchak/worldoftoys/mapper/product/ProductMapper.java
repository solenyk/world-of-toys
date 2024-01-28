package com.kopchak.worldoftoys.mapper.product;

import com.kopchak.worldoftoys.dto.admin.product.AddUpdateProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminFilteredProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminProductsPageDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminProductDto;
import com.kopchak.worldoftoys.dto.product.FilteredProductDto;
import com.kopchak.worldoftoys.dto.product.FilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.product.ProductDto;
import com.kopchak.worldoftoys.dto.image.ImageDto;
import com.kopchak.worldoftoys.domain.product.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public abstract class ProductMapper {
    @Mapping(target = "name", source = "product.name")
    @Mapping(target = "mainImage", source = "mainImage")
    @Mapping(target = "images", source = "images")
    public abstract ProductDto toProductDto(Product product, ImageDto mainImage, List<ImageDto> images);

    @Mapping(target = "name", source = "product.name")
    @Mapping(target = "mainImage", source = "mainImage")
    @Mapping(target = "images", source = "images")
    public abstract AdminProductDto toAdminProductDto(Product product, ImageDto mainImage, List<ImageDto> images);

    @Mapping(target = "originCategory", ignore = true)
    @Mapping(target = "brandCategory", ignore = true)
    @Mapping(target = "ageCategories", ignore = true)
    public abstract Product toProduct(AddUpdateProductDto addUpdateProductDto);

    public FilteredProductsPageDto toFilteredProductsPageDto(Page<Product> productPage) {
        return new FilteredProductsPageDto(toFilteredProductDtoList(productPage.getContent()),
                productPage.getTotalElements(), productPage.getTotalPages());
    }

    public AdminProductsPageDto toAdminFilteredProductsPageDto(Page<Product> productPage) {
        return new AdminProductsPageDto(toAdminFilteredProductDtoList(productPage.getContent()),
                productPage.getTotalElements(), productPage.getTotalPages());
    }

    protected abstract List<FilteredProductDto> toFilteredProductDtoList(List<Product> products);

    protected abstract List<AdminFilteredProductDto> toAdminFilteredProductDtoList(List<Product> products);

}
