package com.kopchak.worldoftoys.dto.user;

import com.kopchak.worldoftoys.exception.validation.ValidationStepThree;
import com.kopchak.worldoftoys.exception.validation.ValidationStepTwo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Username to reset the user's password and resend account activation link")
@GroupSequence({UsernameDto.class, ValidationStepTwo.class, ValidationStepThree.class})
public class UsernameDto {
    @Schema(example = "test@test.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Invalid email: email is empty")
    @Size(min = 6, max = 320, message = "Invalid email: email must be from 6 to 320 characters long",
            groups = ValidationStepTwo.class)
    @Email(regexp = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+" +
            "(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$", message = "Invalid email: email format is incorrect",
            groups = ValidationStepThree.class)
    private String email;
}
