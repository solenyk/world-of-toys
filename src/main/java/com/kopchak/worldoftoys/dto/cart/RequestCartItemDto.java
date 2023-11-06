package com.kopchak.worldoftoys.dto.cart;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "Add, update or delete cart item")
public record RequestCartItemDto(
        @Schema(description = "Unique product identifier in a URL", example = "teddy-bear",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Invalid slug: slug is mandatory")
        @Size(min = 3, max = 80, message = "Slug must be up to 80 characters long") String slug,

        @Schema(description = "Quantity of product in the cart", example = "1")
        @Min(value = 1, message = "Quantity must be greater than or equal to 1") Integer quantity) {
}