package com.kopchak.worldoftoys.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Username to reset the user's password")
public class UsernameDto {
    @Schema(example = "test@test.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @Email(message = "Invalid email")
    @NotBlank(message = "Invalid email: email is empty")
    @NotNull(message = "Invalid email: email is NULL")
    @Size(min = 6, max = 320, message = "Invalid email: email must be up to 320 characters long")
    private String email;
}
