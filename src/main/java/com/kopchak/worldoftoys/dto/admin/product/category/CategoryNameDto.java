package com.kopchak.worldoftoys.dto.admin.product.category;

import com.kopchak.worldoftoys.exception.validation.ValidationStepTwo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Product category name")
@GroupSequence({CategoryNameDto.class, ValidationStepTwo.class})
public record CategoryNameDto(
        @Schema(example = "15-18 років", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Invalid name: name is mandatory")
        @Size(
                min = 3, max = 60,
                message = "Invalid name: name must be up to 60 characters long",
                groups = ValidationStepTwo.class
        ) String name) {
}
