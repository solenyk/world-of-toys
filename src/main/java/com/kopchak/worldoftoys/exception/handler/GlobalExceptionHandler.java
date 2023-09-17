package com.kopchak.worldoftoys.exception.handler;

import com.kopchak.worldoftoys.dto.error.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponseDto> handleResponseStatusException(ResponseStatusException ex) {
        int statusCode = ex.getStatusCode().value();
        ErrorResponseDto errorResponse = ErrorResponseDto
                .builder()
                .error(HttpStatus.valueOf(statusCode).name())
                .status(statusCode)
                .message(ex.getReason())
                .build();
        return ResponseEntity.status(statusCode).body(errorResponse);
    }
}

