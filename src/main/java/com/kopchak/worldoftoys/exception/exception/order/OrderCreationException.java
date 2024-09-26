package com.kopchak.worldoftoys.exception.exception.order;

public class OrderCreationException extends RuntimeException {
    public OrderCreationException(String reason) {
        super(reason);
    }
}
