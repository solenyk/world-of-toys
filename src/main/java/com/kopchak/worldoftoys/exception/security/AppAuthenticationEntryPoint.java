package com.kopchak.worldoftoys.exception.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kopchak.worldoftoys.dto.error.ResponseStatusExceptionDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AppAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) throws IOException {
        HttpStatus httpStatus = HttpStatus.FORBIDDEN;
        ResponseStatusExceptionDto errorResponse = ResponseStatusExceptionDto
                .builder()
                .error(httpStatus.name())
                .status(httpStatus.value())
                .message(ex.getMessage())
                .build();
        response.setStatus(httpStatus.value());
        ObjectMapper mapper = new ObjectMapper();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getWriter(), errorResponse);
    }
}