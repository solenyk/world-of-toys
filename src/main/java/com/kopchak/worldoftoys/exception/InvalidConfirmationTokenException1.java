package com.kopchak.worldoftoys.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InvalidConfirmationTokenException1 extends ResponseStatusException {
    public InvalidConfirmationTokenException1(HttpStatus status, String reason) {
        super(status, reason);
    }
}
