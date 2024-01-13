package com.kopchak.worldoftoys.exception;

public class InvalidConfirmationTokenException extends Exception{
    public InvalidConfirmationTokenException(String reason) {
        super(reason);
    }
}
