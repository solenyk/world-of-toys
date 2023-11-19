package com.kopchak.worldoftoys.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AppStripeException extends ResponseStatusException {
    public AppStripeException(HttpStatus status, String reason) {
        super(status, reason);
    }
}
