package com.kopchak.worldoftoys.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UsernameAlreadyExistException extends ResponseStatusException {
    public UsernameAlreadyExistException(HttpStatus status, String reason) {
        super(status, reason);
    }
}
