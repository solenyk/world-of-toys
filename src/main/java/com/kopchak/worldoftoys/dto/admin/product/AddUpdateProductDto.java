package com.kopchak.worldoftoys.dto.admin.product;

import com.kopchak.worldoftoys.dto.admin.product.category.AdminProductCategoryDto;
import com.kopchak.worldoftoys.exception.validation.ValidationStepTwo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.GroupSequence;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Builder
@Schema(description = "Data to add a new product or update an existing one")
@GroupSequence({AddUpdateProductDto.class, ValidationStepTwo.class})
public record AddUpdateProductDto(
        @Schema(example = "Ведмедик Teddy", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Invalid name: name is mandatory")
        @Size(
                min = 3, max = 60,
                message = "Invalid name: name '${validatedValue}' must be between {min} and {max} characters long",
                groups = ValidationStepTwo.class
        ) String name,

        @Schema(
                example = "М'яка іграшка \"Тедді\" - це невеличкий, приємний на дотик плюшевий ведмідь, " +
                        "який завжди готовий стати вірним другом.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Invalid description: description is mandatory")
        @Size(
                max = 500,
                message = "Invalid description: description '${validatedValue}' must be up to 250 characters long",
                groups = ValidationStepTwo.class
        ) String description,

        @Schema(example = "1500.0", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Invalid price: price is mandatory")
        @DecimalMin(
                value = "0.0", inclusive = false,
                message = "Invalid price: price '${formatter.format('%1$.2f', validatedValue)}' must not be greater than {value}",
                groups = ValidationStepTwo.class
        ) BigDecimal price,

        @Schema(example = "185", requiredMode = Schema.RequiredMode.REQUIRED)
        @Min(
                value = 0,
                message = "Invalid quantity: product quantity '${validatedValue}' must not be less than {value}"
        ) BigInteger availableQuantity,

        @Valid
        @NotNull(message = "Invalid origin category: origin category is mandatory")
        AdminProductCategoryDto originCategory,

        @Valid
        @NotNull(message = "Invalid brand category: brand category is mandatory")
        AdminProductCategoryDto brandCategory,

        @Valid
        @NotNull(message = "Invalid age categories: age categories is mandatory")
        List<AdminProductCategoryDto> ageCategories) {
}
