package com.kopchak.worldoftoys.exception;

public class JwtTokenException extends Exception{
    public JwtTokenException(String reason) {
        super(reason);
    }
}
