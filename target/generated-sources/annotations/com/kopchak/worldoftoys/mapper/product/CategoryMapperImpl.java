package com.kopchak.worldoftoys.mapper.product;

import com.kopchak.worldoftoys.domain.product.category.ProductCategory;
import com.kopchak.worldoftoys.dto.product.category.CategoryDto;
import com.kopchak.worldoftoys.dto.product.category.CategoryDto.CategoryDtoBuilder;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-01-31T22:53:11+0200",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 17.0.7 (Oracle Corporation)"
)
@Component
public class CategoryMapperImpl extends CategoryMapper {

    @Override
    protected List<CategoryDto> toCategoryDtoList(List<ProductCategory> productCategories) {
        if ( productCategories == null ) {
            return null;
        }

        List<CategoryDto> list = new ArrayList<CategoryDto>( productCategories.size() );
        for ( ProductCategory productCategory : productCategories ) {
            list.add( productCategoryToCategoryDto( productCategory ) );
        }

        return list;
    }

    protected CategoryDto productCategoryToCategoryDto(ProductCategory productCategory) {
        if ( productCategory == null ) {
            return null;
        }

        CategoryDtoBuilder categoryDto = CategoryDto.builder();

        categoryDto.name( productCategory.getName() );
        categoryDto.slug( productCategory.getSlug() );

        return categoryDto.build();
    }
}
