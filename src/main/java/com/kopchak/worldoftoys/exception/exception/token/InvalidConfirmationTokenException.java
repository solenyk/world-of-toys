package com.kopchak.worldoftoys.exception.exception.token;

public class InvalidConfirmationTokenException extends RuntimeException {
    public InvalidConfirmationTokenException(String reason) {
        super(reason);
    }
}
