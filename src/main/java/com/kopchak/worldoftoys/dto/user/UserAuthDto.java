package com.kopchak.worldoftoys.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(description = "User data for login")
public record UserAuthDto(
        @Schema(example = "test@test.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Invalid email: email is empty") String email,
        @Schema(example = "P@ssword123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Invalid password: password is empty") String password) {
}
