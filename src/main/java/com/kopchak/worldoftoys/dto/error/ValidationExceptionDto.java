package com.kopchak.worldoftoys.dto.error;

import lombok.Builder;

import java.util.Map;

@Builder
public record ValidationExceptionDto(Map<String, String> fieldsErrorDetails) {
}
