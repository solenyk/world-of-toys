package com.kopchak.worldoftoys.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Set new password")
public class ResetPasswordDto {
    @Schema(example = "password1234", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Invalid password: password is empty")
    @NotNull(message = "Invalid password: password is NULL")
    @Size(min = 8, max = 30, message = "Invalid password: password must from 8 to 30 characters long")
    private String password;
}
