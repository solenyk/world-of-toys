package com.kopchak.worldoftoys.dto.error;

import lombok.Builder;

@Builder
public record ExceptionDto(String message) {
}
