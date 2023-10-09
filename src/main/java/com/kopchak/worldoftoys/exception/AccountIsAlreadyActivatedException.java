package com.kopchak.worldoftoys.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AccountIsAlreadyActivatedException extends ResponseStatusException  {
    public AccountIsAlreadyActivatedException(HttpStatus status, String reason) {
        super(status, reason);
    }
}
