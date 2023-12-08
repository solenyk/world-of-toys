package com.kopchak.worldoftoys.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CategoryNotFoundException extends ResponseStatusException {
    public CategoryNotFoundException(String reason) {
        super(HttpStatus.NOT_FOUND, reason);
    }
}
