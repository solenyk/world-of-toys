package com.kopchak.worldoftoys.exception.exception.token;

public class JwtTokenException extends RuntimeException {
    public JwtTokenException(String reason) {
        super(reason);
    }
}
