package com.kopchak.worldoftoys.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
@Schema(description = "Order recipient phone number data")
public record PhoneNumberDto(
        @Schema(example = "95", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Invalid operator code: code is mandatory")
        @Pattern(regexp = "\\d{2}", message = "Invalid operator code: code '${validatedValue}' format is invalid") String operatorCode,

        @Schema(example = "1234567", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Invalid number: number is mandatory")
        @Pattern(regexp = "\\d{7}", message = "Invalid number: number '${validatedValue}' format is invalid") String number) {
}
