package com.kopchak.worldoftoys.dto.order;

import com.kopchak.worldoftoys.exception.validation.ValidationStepTwo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "Order recipient address data")
@GroupSequence({AddressDto.class, ValidationStepTwo.class})
public record AddressDto(
        @Schema(example = "Закарпатська", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Invalid region: region is mandatory")
        @Size(
                min = 4, max = 16,
                message = "Invalid region: region '${validatedValue}' must be between {min} and {max} characters long",
                groups = ValidationStepTwo.class
        ) String region,

        @Schema(example = "м. Ужгород", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Invalid settlement: settlement is mandatory")
        @Size(
                min = 2, max = 60,
                message = "Invalid settlement: settlement '${validatedValue}' must be between {min} and {max} characters long",
                groups = ValidationStepTwo.class
        ) String settlement,

        @Schema(example = "вул. Грушевського", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Invalid street: street is mandatory")
        @Size(
                min = 2, max = 80,
                message = "Invalid street: street '${validatedValue}' must be between {min} and {max} characters long",
                groups = ValidationStepTwo.class
        ) String street,

        @Schema(example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message =  "Invalid house: house is mandatory")
        @Min(
                value = 1,
                message = "Invalid house number: house number '${validatedValue}' should not be less than {value}",
                groups = ValidationStepTwo.class
        ) Integer house,

        @Schema(example = "8", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Min(
                value = 1,
                message = "Invalid apartment number: apartment number '${validatedValue}' should not be less than {value}"
        ) Integer apartment) {
}
