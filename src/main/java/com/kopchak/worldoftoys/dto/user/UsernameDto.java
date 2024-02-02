package com.kopchak.worldoftoys.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(description = "Username to reset the user's password and resend account activation link")
public record UsernameDto(
        @Schema(example = "test@test.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Invalid email: email is empty") String email) {
}
