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
@Schema(description = "User data for registration")
public class UserRegistrationDto {

    @Schema(example = "Iryna", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Invalid firstname: firstname is empty")
    @NotNull(message = "Invalid firstname: firstname is NULL")
    @Size(min = 3, max = 60, message = "Invalid firstname: firstname must be up to 60 characters long")
    private String firstname;

    @Schema(example = "Kopchak", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Invalid lastname: lastname is empty")
    @NotNull(message = "Invalid lastname: lastname is NULL")
    @Size(min = 3, max = 60, message = "Invalid lastname: lastname must be up to 60 characters long")
    private String lastname;

    @Schema(example = "test@test.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @Email(message = "Invalid email")
    @NotBlank(message = "Invalid email: email is empty")
    @NotNull(message = "Invalid email: email is NULL")
    @Size(min = 6, max = 320, message = "Invalid email: email must be up to 320 characters long")
    private String email;

    @Schema(example = "password1234", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Invalid password: password is empty")
    @NotNull(message = "Invalid password: password is NULL")
    @Size(min = 8, max = 30, message = "Invalid password: password must from 8 to 30 characters long")
    private String password;
}
