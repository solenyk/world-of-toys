package com.kopchak.worldoftoys.exception.exception.user;

public class UsernameAlreadyExistException extends Exception {
    public UsernameAlreadyExistException(String reason) {
        super(reason);
    }
}
