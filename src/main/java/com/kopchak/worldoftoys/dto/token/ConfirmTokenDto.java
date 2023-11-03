package com.kopchak.worldoftoys.dto.token;

import com.kopchak.worldoftoys.model.token.ConfirmationToken;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Confirmation token")
public class ConfirmTokenDto {
    @NotBlank(message = "Invalid token: token is empty")
    private String token;

    public ConfirmTokenDto(ConfirmationToken confirmToken) {
        this.token = confirmToken.getToken();
    }
}
