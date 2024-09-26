package com.kopchak.worldoftoys.exception.exception.category;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String reason) {
        super(reason);
    }
}
