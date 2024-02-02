package com.kopchak.worldoftoys.exception.exception.user;

public class UserNotFoundException extends Exception {
    public UserNotFoundException(String reason) {
        super(reason);
    }
}
