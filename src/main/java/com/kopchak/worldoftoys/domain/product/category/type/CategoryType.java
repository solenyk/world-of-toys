package com.kopchak.worldoftoys.domain.product.category.type;

import com.kopchak.worldoftoys.domain.product.category.AgeCategory;
import com.kopchak.worldoftoys.domain.product.category.BrandCategory;
import com.kopchak.worldoftoys.domain.product.category.OriginCategory;
import com.kopchak.worldoftoys.domain.product.category.ProductCategory;
import lombok.Getter;

@Getter
public enum CategoryType {
    BRANDS(BrandCategory.class),
    ORIGINS(OriginCategory.class),
    AGES(AgeCategory.class);

    private final Class<? extends ProductCategory> category;

    CategoryType(Class<? extends ProductCategory> category) {
        this.category = category;
    }
}
