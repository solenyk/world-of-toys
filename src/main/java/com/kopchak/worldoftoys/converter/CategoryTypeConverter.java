package com.kopchak.worldoftoys.converter;

import com.kopchak.worldoftoys.domain.product.category.type.CategoryType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CategoryTypeConverter implements Converter<String, CategoryType> {
    @Override
    public CategoryType convert(String source) {
        return CategoryType.valueOf(source.toUpperCase());
    }
}
