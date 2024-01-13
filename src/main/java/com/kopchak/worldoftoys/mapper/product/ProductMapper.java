package com.kopchak.worldoftoys.mapper.product;

import com.kopchak.worldoftoys.dto.admin.product.AddUpdateProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminFilteredProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminFilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminProductDto;
import com.kopchak.worldoftoys.dto.admin.product.category.AdminProductCategoryIdDto;
import com.kopchak.worldoftoys.dto.image.ImageDto;
import com.kopchak.worldoftoys.dto.product.FilteredProductDto;
import com.kopchak.worldoftoys.dto.product.FilteredProductsPageDto;
import com.kopchak.worldoftoys.dto.product.ProductDto;
import com.kopchak.worldoftoys.exception.exception.CategoryException;
import com.kopchak.worldoftoys.exception.exception.ImageCompressionException;
import com.kopchak.worldoftoys.exception.exception.ImageExceedsMaxSizeException;
import com.kopchak.worldoftoys.exception.exception.InvalidImageFileFormatException;
import com.kopchak.worldoftoys.model.image.Image;
import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.model.product.category.AgeCategory;
import com.kopchak.worldoftoys.model.product.category.BrandCategory;
import com.kopchak.worldoftoys.model.product.category.OriginCategory;
import com.kopchak.worldoftoys.repository.product.ProductCategoryRepository;
import com.kopchak.worldoftoys.service.ImageService;
import org.mapstruct.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {ProductCategoryMapper.class})
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

    protected abstract List<FilteredProductDto> toFilteredProductDtoList(List<Product> products);

    protected abstract List<AdminFilteredProductDto> toAdminFilteredProductDtoList(List<Product> products);

    public FilteredProductsPageDto toFilteredProductsPageDto(Page<Product> productPage) {
        return new FilteredProductsPageDto(toFilteredProductDtoList(productPage.getContent()),
                productPage.getTotalElements(), productPage.getTotalPages());
    }

    public AdminFilteredProductsPageDto toAdminFilteredProductsPageDto(Page<Product> productPage) {
        return new AdminFilteredProductsPageDto(toAdminFilteredProductDtoList(productPage.getContent()),
                productPage.getTotalElements(), productPage.getTotalPages());
    }
}
