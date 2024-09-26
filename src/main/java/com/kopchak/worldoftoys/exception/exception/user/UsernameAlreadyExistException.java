package com.kopchak.worldoftoys.exception.exception.user;

public class UsernameAlreadyExistException extends RuntimeException {
    public UsernameAlreadyExistException(String reason) {
        super(reason);
    }
}
