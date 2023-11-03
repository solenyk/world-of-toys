package com.kopchak.worldoftoys.dto.user;

import com.kopchak.worldoftoys.exception.validation.ValidationStepThree;
import com.kopchak.worldoftoys.exception.validation.ValidationStepTwo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.GroupSequence;
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
@Schema(description = "User data for login")
@GroupSequence({UserAuthDto.class, ValidationStepTwo.class, ValidationStepThree.class})
public class UserAuthDto {
    @Schema(example = "test@test.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Invalid email: email is empty")
    @Size(min = 6, max = 320, message = "Invalid email: email must be from 6 to 320 characters long",
            groups = ValidationStepTwo.class)
    @Email(regexp = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+" +
            "(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$", message = "Invalid email: email format is incorrect",
            groups = ValidationStepThree.class)
    private String email;

    @Schema(example = "password1234", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Invalid password: password is empty")
    @Size(min = 8, max = 30, message = "Invalid password: password must from 8 to 30 characters long",
            groups = ValidationStepTwo.class)
    private String password;
}
