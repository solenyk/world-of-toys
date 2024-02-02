package com.kopchak.worldoftoys.mapper.product;

import com.kopchak.worldoftoys.domain.product.Product;
import com.kopchak.worldoftoys.domain.product.Product.ProductBuilder;
import com.kopchak.worldoftoys.domain.product.category.AgeCategory;
import com.kopchak.worldoftoys.domain.product.category.BrandCategory;
import com.kopchak.worldoftoys.domain.product.category.OriginCategory;
import com.kopchak.worldoftoys.dto.admin.product.AddUpdateProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminFilteredProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminFilteredProductDto.AdminFilteredProductDtoBuilder;
import com.kopchak.worldoftoys.dto.admin.product.AdminProductDto;
import com.kopchak.worldoftoys.dto.admin.product.AdminProductDto.AdminProductDtoBuilder;
import com.kopchak.worldoftoys.dto.image.ImageDto;
import com.kopchak.worldoftoys.dto.product.FilteredProductDto;
import com.kopchak.worldoftoys.dto.product.FilteredProductDto.FilteredProductDtoBuilder;
import com.kopchak.worldoftoys.dto.product.ProductDto;
import com.kopchak.worldoftoys.dto.product.ProductDto.ProductDtoBuilder;
import com.kopchak.worldoftoys.dto.product.category.CategoryDto;
import com.kopchak.worldoftoys.dto.product.category.CategoryDto.CategoryDtoBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-01-31T23:11:18+0200",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 17.0.7 (Oracle Corporation)"
)
@Component
public class ProductMapperImpl extends ProductMapper {

    @Override
    public ProductDto toProductDto(Product product, ImageDto mainImage, List<ImageDto> images) {
        if ( product == null && mainImage == null && images == null ) {
            return null;
        }

        ProductDtoBuilder productDto = ProductDto.builder();

        if ( product != null ) {
            productDto.name( product.getName() );
            productDto.slug( product.getSlug() );
            productDto.description( product.getDescription() );
            productDto.price( product.getPrice() );
            productDto.availableQuantity( product.getAvailableQuantity() );
            productDto.originCategory( originCategoryToCategoryDto( product.getOriginCategory() ) );
            productDto.brandCategory( brandCategoryToCategoryDto( product.getBrandCategory() ) );
            productDto.ageCategories( ageCategorySetToCategoryDtoList( product.getAgeCategories() ) );
        }
        if ( mainImage != null ) {
            productDto.mainImage( mainImage );
        }
        if ( images != null ) {
            List<ImageDto> list = images;
            if ( list != null ) {
                productDto.images( new ArrayList<ImageDto>( list ) );
            }
        }

        return productDto.build();
    }

    @Override
    public AdminProductDto toAdminProductDto(Product product, ImageDto mainImage, List<ImageDto> images) {
        if ( product == null && mainImage == null && images == null ) {
            return null;
        }

        AdminProductDtoBuilder adminProductDto = AdminProductDto.builder();

        if ( product != null ) {
            adminProductDto.name( product.getName() );
            adminProductDto.id( product.getId() );
            adminProductDto.slug( product.getSlug() );
            adminProductDto.description( product.getDescription() );
            adminProductDto.price( product.getPrice() );
            adminProductDto.availableQuantity( product.getAvailableQuantity() );
            adminProductDto.isAvailable( product.getIsAvailable() );
            adminProductDto.originCategory( originCategoryToCategoryDto( product.getOriginCategory() ) );
            adminProductDto.brandCategory( brandCategoryToCategoryDto( product.getBrandCategory() ) );
            adminProductDto.ageCategories( ageCategorySetToCategoryDtoList( product.getAgeCategories() ) );
        }
        if ( mainImage != null ) {
            adminProductDto.mainImage( mainImage );
        }
        if ( images != null ) {
            List<ImageDto> list = images;
            if ( list != null ) {
                adminProductDto.images( new ArrayList<ImageDto>( list ) );
            }
        }

        return adminProductDto.build();
    }

    @Override
    public Product toProduct(AddUpdateProductDto addUpdateProductDto) {
        if ( addUpdateProductDto == null ) {
            return null;
        }

        ProductBuilder product = Product.builder();

        product.name( addUpdateProductDto.name() );
        product.description( addUpdateProductDto.description() );
        product.price( addUpdateProductDto.price() );
        product.isAvailable( addUpdateProductDto.isAvailable() );
        product.availableQuantity( addUpdateProductDto.availableQuantity() );

        return product.build();
    }

    @Override
    public FilteredProductDto toFilteredProductDto(Product product, ImageDto mainImage) {
        if ( product == null && mainImage == null ) {
            return null;
        }

        FilteredProductDtoBuilder filteredProductDto = FilteredProductDto.builder();

        if ( product != null ) {
            filteredProductDto.name( product.getName() );
            filteredProductDto.slug( product.getSlug() );
            filteredProductDto.price( product.getPrice() );
            filteredProductDto.availableQuantity( product.getAvailableQuantity() );
        }
        if ( mainImage != null ) {
            filteredProductDto.mainImage( mainImage );
        }

        return filteredProductDto.build();
    }

    @Override
    public AdminFilteredProductDto toAdminFilteredProductDto(Product product, ImageDto mainImage) {
        if ( product == null && mainImage == null ) {
            return null;
        }

        AdminFilteredProductDtoBuilder adminFilteredProductDto = AdminFilteredProductDto.builder();

        if ( product != null ) {
            adminFilteredProductDto.name( product.getName() );
            adminFilteredProductDto.id( product.getId() );
            adminFilteredProductDto.price( product.getPrice() );
            adminFilteredProductDto.availableQuantity( product.getAvailableQuantity() );
            adminFilteredProductDto.isAvailable( product.getIsAvailable() );
        }
        if ( mainImage != null ) {
            adminFilteredProductDto.mainImage( mainImage );
        }

        return adminFilteredProductDto.build();
    }

    protected CategoryDto originCategoryToCategoryDto(OriginCategory originCategory) {
        if ( originCategory == null ) {
            return null;
        }

        CategoryDtoBuilder categoryDto = CategoryDto.builder();

        categoryDto.name( originCategory.getName() );
        categoryDto.slug( originCategory.getSlug() );

        return categoryDto.build();
    }

    protected CategoryDto brandCategoryToCategoryDto(BrandCategory brandCategory) {
        if ( brandCategory == null ) {
            return null;
        }

        CategoryDtoBuilder categoryDto = CategoryDto.builder();

        categoryDto.name( brandCategory.getName() );
        categoryDto.slug( brandCategory.getSlug() );

        return categoryDto.build();
    }

    protected CategoryDto ageCategoryToCategoryDto(AgeCategory ageCategory) {
        if ( ageCategory == null ) {
            return null;
        }

        CategoryDtoBuilder categoryDto = CategoryDto.builder();

        categoryDto.name( ageCategory.getName() );
        categoryDto.slug( ageCategory.getSlug() );

        return categoryDto.build();
    }

    protected List<CategoryDto> ageCategorySetToCategoryDtoList(Set<AgeCategory> set) {
        if ( set == null ) {
            return null;
        }

        List<CategoryDto> list = new ArrayList<CategoryDto>( set.size() );
        for ( AgeCategory ageCategory : set ) {
            list.add( ageCategoryToCategoryDto( ageCategory ) );
        }

        return list;
    }
}
