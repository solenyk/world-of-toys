package com.kopchak.worldoftoys.dto.user;

import com.kopchak.worldoftoys.exception.validation.ValidationStepTwo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "User data for registration")
@GroupSequence({UserRegistrationDto.class, ValidationStepTwo.class})
public record UserRegistrationDto(
        @Schema(example = "Iryna", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Invalid firstname: firstname is empty")
        @Size(
                min = 3,
                max = 60,
                message = "Invalid firstname: firstname '${validatedValue}' must be up from {min} to {max}" +
                        " characters long", groups = ValidationStepTwo.class
        ) String firstname,

        @Schema(example = "Kopchak", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Invalid lastname: lastname is empty")
        @Size(
                min = 3,
                max = 60,
                message = "Invalid lastname: lastname '${validatedValue}' must be from {min} to {max} characters long",
                groups = ValidationStepTwo.class
        ) String lastname,

        @Schema(example = "test@test.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Invalid email: email is empty")
        @Email(regexp = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+" +
                "(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$",
                message = "Invalid email: email '${validatedValue}' format is incorrect",
                groups = ValidationStepTwo.class) String email,

        @Schema(example = "P@ssword123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Invalid password: password is empty")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[#$@!%&*?])[A-Za-z\\d#$@!%&*?]{8,16}$",
                message = "Invalid password: password must contain at least 1 number (0-9), 1 uppercase letter, " +
                        "1 lowercase letter, 1 non-alphanumeric number and be 8-16 characters with no space",
                groups = ValidationStepTwo.class) String password) {
}
