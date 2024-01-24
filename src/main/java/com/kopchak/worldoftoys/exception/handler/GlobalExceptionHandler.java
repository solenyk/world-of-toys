package com.kopchak.worldoftoys.exception.handler;

import com.kopchak.worldoftoys.dto.error.MethodArgumentNotValidExceptionDto;
import com.kopchak.worldoftoys.dto.error.ResponseStatusExceptionDto;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ResponseStatusExceptionDto> handleJwtTokenException(ResponseStatusException e) {
        int statusCode = e.getStatusCode().value();
        HttpStatus httpStatus = HttpStatus.valueOf(statusCode);
        var errorResponse = new ResponseStatusExceptionDto(statusCode, httpStatus.name(), e.getReason());
        return ResponseEntity.status(statusCode).body(errorResponse);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public MethodArgumentNotValidExceptionDto handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> fieldsErrorDetails = new LinkedHashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldsErrorDetails.put(fieldName, errorMessage);
        });
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        return new MethodArgumentNotValidExceptionDto(httpStatus.value(), httpStatus.name(),
                fieldsErrorDetails);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConversionFailedException.class)
    public ResponseStatusExceptionDto handleConversionFailedException(ConversionFailedException e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        return new ResponseStatusExceptionDto(httpStatus.value(), httpStatus.name(), e.getMessage());
    }
}

