package com.kopchak.worldoftoys.exception.handler;

import com.kopchak.worldoftoys.dto.error.ValidationExceptionDto;
import com.kopchak.worldoftoys.dto.error.ExceptionDto;
import com.kopchak.worldoftoys.exception.exception.cart.CartValidationException;
import com.kopchak.worldoftoys.exception.exception.category.CategoryContainsProductsException;
import com.kopchak.worldoftoys.exception.exception.category.CategoryCreationException;
import com.kopchak.worldoftoys.exception.exception.category.CategoryNotFoundException;
import com.kopchak.worldoftoys.exception.exception.category.DuplicateCategoryNameException;
import com.kopchak.worldoftoys.exception.exception.email.MessageSendingException;
import com.kopchak.worldoftoys.exception.exception.image.InvalidImageFileFormatException;
import com.kopchak.worldoftoys.exception.exception.order.OrderNotFoundException;
import com.kopchak.worldoftoys.exception.exception.order.InvalidOrderStatusException;
import com.kopchak.worldoftoys.exception.exception.order.OrderCreationException;
import com.kopchak.worldoftoys.exception.exception.product.DuplicateProductNameException;
import com.kopchak.worldoftoys.exception.exception.product.ProductNotFoundException;
import com.kopchak.worldoftoys.exception.exception.token.InvalidConfirmationTokenException;
import com.kopchak.worldoftoys.exception.exception.token.JwtTokenException;
import com.kopchak.worldoftoys.exception.exception.token.TokenAlreadyExistException;
import com.kopchak.worldoftoys.exception.exception.user.AccountActivationException;
import com.kopchak.worldoftoys.exception.exception.user.InvalidPasswordException;
import com.kopchak.worldoftoys.exception.exception.user.UserNotFoundException;
import com.kopchak.worldoftoys.exception.exception.user.UsernameAlreadyExistException;
import com.stripe.exception.StripeException;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.kopchak.worldoftoys.exception.exception.image.ImageCompressionException;
import com.kopchak.worldoftoys.exception.exception.image.ImageExceedsMaxSizeException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            CartValidationException.class, CategoryContainsProductsException.class, CategoryCreationException.class,
            ImageCompressionException.class, ImageExceedsMaxSizeException.class, InvalidOrderStatusException.class,
            OrderCreationException.class, InvalidConfirmationTokenException.class, JwtTokenException.class,
            AccountActivationException.class, InvalidPasswordException.class, TokenAlreadyExistException.class,
            ConversionFailedException.class
    })
    public ExceptionDto handleBadRequestException(ResponseStatusException e) {
        return new ExceptionDto(e.getReason());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({ProductNotFoundException.class, CategoryNotFoundException.class, UserNotFoundException.class,
            OrderNotFoundException.class})
    public ExceptionDto handleNotFoundException(RuntimeException e) {
        return new ExceptionDto(e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({
            DuplicateProductNameException.class, DuplicateCategoryNameException.class, UsernameAlreadyExistException.class
    })
    public ExceptionDto handleConflictException(RuntimeException e) {
        return new ExceptionDto(e.getMessage());
    }

    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(InvalidImageFileFormatException.class)
    public ExceptionDto handleUnsupportedMediaTypeException(RuntimeException e) {
        return new ExceptionDto(e.getMessage());
    }

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(MessageSendingException.class)
    public ExceptionDto handleServiceUnavailableException(RuntimeException e) {
        return new ExceptionDto(e.getMessage());
    }

    @ExceptionHandler(StripeException.class)
    public ResponseEntity<ExceptionDto> handleStripeException(StripeException e) {
        return ResponseEntity.status(e.getStatusCode()).body(new ExceptionDto(e.getMessage()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ValidationExceptionDto handleValidationExceptions(MethodArgumentNotValidException e) {
//        Map<String, String> fieldsErrorDetails = new LinkedHashMap<>();
//        e.getBindingResult().getAllErrors().forEach((error) -> {
//            String fieldName = ((FieldError) error).getField();
//            String errorMessage = error.getDefaultMessage();
//            fieldsErrorDetails.put(fieldName, errorMessage);
//        });

        Map<String, String> fieldsErrorDetails = e.getBindingResult().getFieldErrors()
                .stream()
                .filter(fieldError -> fieldError.getDefaultMessage() != null)
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
        return new ValidationExceptionDto(fieldsErrorDetails);
    }
}

