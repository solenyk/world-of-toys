package com.kopchak.worldoftoys.exception.exception;

public class ProductNotFoundException extends Exception{
    public ProductNotFoundException(String reason) {
        super(reason);
    }
}
