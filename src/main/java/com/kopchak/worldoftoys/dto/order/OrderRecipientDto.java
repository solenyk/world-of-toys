package com.kopchak.worldoftoys.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "Order recipient data")
public record OrderRecipientDto(
        @Schema(example = "Копчак", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Invalid lastname: lastname is blank")
        @Size(
                min = 3, max = 60,
                message = "Invalid lastname: lastname '${validatedValue}' must be from {min} to {max} characters long"
        ) String lastname,

        @Schema(example = "Ірина", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Invalid firstname: firstname is blank")
        @Size(
                min = 3, max = 60,
                message = "Invalid firstname: firstname '${validatedValue}' must be from {min} to {max} characters long"
        ) String firstname,

        @Schema(example = "Сергіївна", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Invalid patronymic: patronymic is blank")
        @Size(
                min = 3, max = 60,
                message = "Invalid patronymic: patronymic '${validatedValue}' must be from {min} to {max} characters long"
        ) String patronymic,

        @Valid
        @NotNull(message = "Invalid phone number: phone number is mandatory") PhoneNumberDto phoneNumber,

        @Valid
        @NotNull(message = "Invalid address: address is mandatory") AddressDto address) {
}
