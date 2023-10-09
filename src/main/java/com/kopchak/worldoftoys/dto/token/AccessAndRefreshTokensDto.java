package com.kopchak.worldoftoys.dto.token;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccessAndRefreshTokensDto {
    @NotBlank(message = "Invalid access token: token is empty")
    @NotNull(message = "Invalid access token: token is NULL")
    private String accessToken;

    @NotBlank(message = "Invalid refresh token: token is empty")
    @NotNull(message = "Invalid refresh token: token is NULL")
    private String refreshToken;
}
