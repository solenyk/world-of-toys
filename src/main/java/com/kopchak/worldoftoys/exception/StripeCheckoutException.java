package com.kopchak.worldoftoys.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class StripeCheckoutException extends ResponseStatusException {
    public StripeCheckoutException(HttpStatus status, String reason) {
        super(status, reason);
    }
}
