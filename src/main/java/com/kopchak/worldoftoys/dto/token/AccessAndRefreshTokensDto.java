package com.kopchak.worldoftoys.dto.token;

import com.kopchak.worldoftoys.dto.user.UsernameDto;
import com.kopchak.worldoftoys.exception.validation.ValidationStepThree;
import com.kopchak.worldoftoys.exception.validation.ValidationStepTwo;
import jakarta.validation.GroupSequence;
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
    private String accessToken;

    @NotBlank(message = "Invalid refresh token: token is empty")
    private String refreshToken;
}
