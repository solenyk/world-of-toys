package com.kopchak.worldoftoys.dto.admin.product;

import com.kopchak.worldoftoys.dto.admin.product.category.AdminProductCategoryDto;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Builder
public record UpdateProductDto(
        @NotBlank(message = "Invalid name: name is mandatory")
        @Size(min = 3, max = 60, message = "Invalid name: name '${validatedValue}' must be between {min} and {max} characters long")
        String name,

        @NotBlank(message = "Invalid description: description is mandatory")
        @Size(max = 500, message = "Invalid description: description '${validatedValue}' must be up to 250 characters long")
        String description,

        @NotNull(message = "Invalid price: price is mandatory")
        @DecimalMin(value = "0.0", inclusive = false,
                message = "Invalid price: price '${formatter.format('%1$.2f', validatedValue)}' must not be greater than {value}") BigDecimal price,

        @Min(value = 0, message = "Invalid quantity: product quantity '${validatedValue}' must not be less than {value}")
        BigInteger availableQuantity,

        @NotNull(message = "Invalid origin category: origin category is mandatory")
        AdminProductCategoryDto originCategory,

        @NotNull(message = "Invalid brand category: brand category is mandatory")
        AdminProductCategoryDto brandCategory,

        @NotNull(message = "Invalid age categories: age categories is mandatory")
        List<AdminProductCategoryDto> ageCategories) {
}
