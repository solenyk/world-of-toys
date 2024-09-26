package com.kopchak.worldoftoys.exception.exception.product;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String reason) {
        super(reason);
    }
}
