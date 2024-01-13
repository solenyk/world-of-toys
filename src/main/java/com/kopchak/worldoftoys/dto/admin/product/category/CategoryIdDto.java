package com.kopchak.worldoftoys.dto.admin.product.category;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(description = "Product category id")
public record CategoryIdDto(
        @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @Min(
                value = 1,
                message = "Invalid product category id: id '${validatedValue}' should not be less than {status}"
        )Integer id
) {
}
