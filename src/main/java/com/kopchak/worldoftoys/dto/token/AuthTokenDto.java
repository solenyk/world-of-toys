package com.kopchak.worldoftoys.dto.token;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Access token")
public class AuthTokenDto {
    @NotBlank(message = "Invalid token: token is empty")
    private String token;
}
