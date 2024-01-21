package com.kopchak.worldoftoys.exception;

public class CategoryNotFoundException extends Exception {
    public CategoryNotFoundException(String reason) {
        super(reason);
    }
}
