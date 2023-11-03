package com.kopchak.worldoftoys.dto.user;

import com.kopchak.worldoftoys.exception.validation.ValidationStepThree;
import com.kopchak.worldoftoys.exception.validation.ValidationStepTwo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "User data for registration")
@GroupSequence({UserRegistrationDto.class, ValidationStepTwo.class, ValidationStepThree.class})
public class UserRegistrationDto {
    @Schema(example = "Iryna", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Invalid firstname: firstname is empty")
    @Size(min = 3, max = 60, message = "Invalid firstname: firstname must be up to 60 characters long",
            groups = ValidationStepTwo.class)
    private final String firstname;

    @Schema(example = "Kopchak", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Invalid lastname: lastname is empty")
    @Size(min = 3, max = 60, message = "Invalid lastname: lastname must be up to 60 characters long",
            groups = ValidationStepTwo.class)
    private final String lastname;

    @Schema(example = "test@test.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Invalid email: email is empty")
    @Size(min = 6, max = 320, message = "Invalid email: email must be from 6 to 320 characters long",
            groups = ValidationStepTwo.class)
    @Email(regexp = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+" +
            "(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$", message = "Invalid email: email format is incorrect",
            groups = ValidationStepThree.class)
    private final String email;

    @Schema(example = "password1234", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Invalid password: password is empty")
    @Size(min = 8, max = 30, message = "Invalid password: password must from 8 to 30 characters long",
            groups = ValidationStepTwo.class)
    private final String password;
}
