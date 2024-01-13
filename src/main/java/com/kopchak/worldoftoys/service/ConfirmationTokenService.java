package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.token.ConfirmTokenDto;
import com.kopchak.worldoftoys.dto.user.ResetPasswordDto;
import com.kopchak.worldoftoys.exception.*;
import com.kopchak.worldoftoys.domain.token.ConfirmationTokenType;

public interface ConfirmationTokenService {
    ConfirmTokenDto createConfirmationToken(String username, ConfirmationTokenType tokenType)
            throws UserNotFoundException, TokenAlreadyExistException, AccountActivationException;

    void activateAccountUsingActivationToken(String token) throws InvalidConfirmationTokenException;

    void changePasswordUsingResetToken(String token, ResetPasswordDto newPassword)
            throws InvalidConfirmationTokenException, InvalidPasswordException;
}
