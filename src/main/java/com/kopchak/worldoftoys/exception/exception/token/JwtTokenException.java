package com.kopchak.worldoftoys.exception.exception.token;

public class JwtTokenException extends Exception {
    public JwtTokenException(String reason) {
        super(reason);
    }
}
