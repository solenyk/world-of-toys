package com.kopchak.worldoftoys.domain.product.category.type;

import com.kopchak.worldoftoys.exception.CategoryNotFoundException;
import lombok.Getter;

@Getter
public enum CategoryType {
    BRANDS("brands"),
    ORIGINS("origins"),
    AGES("ages");

    private final String value;

    CategoryType(String value) {
        this.value = value;
    }

    public static CategoryType findByValue(String value) throws CategoryNotFoundException {
        for (CategoryType categoryType : CategoryType.values()) {
            if (categoryType.value.equalsIgnoreCase(value)) {
                return categoryType;
            }
        }
        throw new CategoryNotFoundException(String.format("Category: %s does not exist", value));
    }
}
