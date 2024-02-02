package com.kopchak.worldoftoys.dto.token;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(description = "Access token")
public record AuthTokenDto(@NotBlank(message = "Invalid token: token is empty") String token) {
}
