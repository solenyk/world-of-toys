package com.kopchak.worldoftoys.exception.exception;

public class UserNotFoundException extends Exception{
    public UserNotFoundException(String reason) {
        super(reason);
    }
}
