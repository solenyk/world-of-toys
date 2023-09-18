package com.kopchak.worldoftoys.dto.token;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthTokenDto {
    @NotBlank(message = "Invalid token: token is empty")
    @NotNull(message = "Invalid token: token is NULL")
    private String token;
}
