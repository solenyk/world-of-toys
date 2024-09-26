package com.kopchak.worldoftoys.exception.exception.user;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String reason) {
        super(reason);
    }
}
