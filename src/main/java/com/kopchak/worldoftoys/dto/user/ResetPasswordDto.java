package com.kopchak.worldoftoys.dto.user;

import com.kopchak.worldoftoys.exception.validation.ValidationStepTwo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
@Schema(description = "Set new password")
@GroupSequence({ResetPasswordDto.class, ValidationStepTwo.class})
public record ResetPasswordDto(
        @Schema(example = "P@ssword123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Invalid password: password is empty")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[#$@!%&*?])[A-Za-z\\d#$@!%&*?]{8,16}$",
                message = "Invalid password: password must contain at least 1 number (0-9), 1 uppercase letter, " +
                        "1 lowercase letter, 1 non-alphanumeric number and be 8-16 characters with no space",
                groups = ValidationStepTwo.class) String password) {
}
