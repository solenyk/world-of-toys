package com.kopchak.worldoftoys.dto.token;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(description = "Access and refresh tokens")
public record AccessAndRefreshTokensDto(@NotBlank(message = "Invalid access token: token is empty") String accessToken,
                                        @NotBlank(message = "Invalid refresh token: token is empty") String refreshToken) {
}
