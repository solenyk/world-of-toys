package com.kopchak.worldoftoys.dto.admin.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Order or payment status data")
public record StatusDto(
        @Schema(example = "CANCELED", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Invalid name: name is mandatory")
        String name,
        @Schema(example = "Canceled", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Invalid status: status is mandatory")
        String status
) {
}
