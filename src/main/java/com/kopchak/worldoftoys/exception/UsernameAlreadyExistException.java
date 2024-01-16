package com.kopchak.worldoftoys.exception;

public class UsernameAlreadyExistException extends Exception {
    public UsernameAlreadyExistException(String reason) {
        super(reason);
    }
}
