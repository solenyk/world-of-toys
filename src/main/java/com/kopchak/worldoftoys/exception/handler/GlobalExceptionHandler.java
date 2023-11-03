package com.kopchak.worldoftoys.exception.handler;

import com.kopchak.worldoftoys.dto.error.ResponseStatusExceptionDto;
import com.kopchak.worldoftoys.dto.error.MethodArgumentNotValidExceptionDto;
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
    public ResponseEntity<ResponseStatusExceptionDto> handleResponseStatusException(ResponseStatusException ex) {
        int statusCode = ex.getStatusCode().value();
        HttpStatus httpStatus = HttpStatus.valueOf(statusCode);
        ResponseStatusExceptionDto errorResponse = new ResponseStatusExceptionDto(statusCode, httpStatus.name(),
                ex.getReason());
        return ResponseEntity.status(statusCode).body(errorResponse);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public MethodArgumentNotValidExceptionDto handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldsErrorDetails = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldsErrorDetails.put(fieldName, errorMessage);
        });
        return new MethodArgumentNotValidExceptionDto(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(),
                fieldsErrorDetails);
    }
}

