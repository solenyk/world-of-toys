package com.kopchak.worldoftoys.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class IncorrectPasswordException extends ResponseStatusException {
    public IncorrectPasswordException(HttpStatus status, String reason) {
        super(status, reason);
    }
}
