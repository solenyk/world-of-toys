package com.kopchak.worldoftoys.dto.error;

import lombok.Builder;

import java.util.Map;

@Builder
public record MethodArgumentNotValidExceptionDto(int status, String error, Map<String, String> fieldsErrorDetails) {
}
