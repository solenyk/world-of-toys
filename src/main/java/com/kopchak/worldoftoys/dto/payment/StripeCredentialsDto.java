package com.kopchak.worldoftoys.dto.payment;

import com.kopchak.worldoftoys.exception.validation.ValidationStepTwo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "User credentials for stripe checkout")
@GroupSequence({StripeCredentialsDto.class, ValidationStepTwo.class})
public record StripeCredentialsDto(
        @Schema(example = "iryna", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @NotBlank(message = "Invalid customer name: customer name is empty") String customerName,
        @Schema(example = "test@test.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Invalid email: email is empty")
        @Email(regexp = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+" +
                "(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$",
                message = "Invalid email: email '${validatedValue}' format is incorrect",
                groups = ValidationStepTwo.class) String customerEmail) {
}
