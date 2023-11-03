package com.kopchak.worldoftoys.dto.error;

import lombok.Builder;

@Builder
public record ResponseStatusExceptionDto(int status, String error, String message) {
}
