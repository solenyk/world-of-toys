package com.kopchak.worldoftoys.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UserNotFoundException1 extends ResponseStatusException {
    public UserNotFoundException1(HttpStatus status, String reason) {
        super(status, reason);
    }
}
