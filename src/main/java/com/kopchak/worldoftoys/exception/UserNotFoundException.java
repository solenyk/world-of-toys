package com.kopchak.worldoftoys.exception;

public class UserNotFoundException extends Exception{
    public UserNotFoundException(String reason) {
        super(reason);
    }
}
