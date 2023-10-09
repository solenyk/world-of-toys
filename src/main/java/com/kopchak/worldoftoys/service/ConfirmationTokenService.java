package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.token.ConfirmTokenDto;
import com.kopchak.worldoftoys.dto.user.ResetPasswordDto;
import com.kopchak.worldoftoys.model.token.ConfirmationTokenType;

public interface ConfirmationTokenService {
    ConfirmTokenDto createConfirmationToken(String username, ConfirmationTokenType tokenType);
    boolean isConfirmationTokenInvalid(String token, ConfirmationTokenType tokenType);
    void activateAccountUsingActivationToken(String token);
    boolean isNoActiveConfirmationToken(String email, ConfirmationTokenType confirmTokenType);
    void changePasswordUsingResetToken(String token, ResetPasswordDto newPassword);
}
