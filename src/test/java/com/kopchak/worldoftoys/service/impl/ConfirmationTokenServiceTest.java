package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.domain.token.confirm.ConfirmationToken;
import com.kopchak.worldoftoys.domain.token.confirm.ConfirmationTokenType;
import com.kopchak.worldoftoys.domain.user.AppUser;
import com.kopchak.worldoftoys.dto.user.ResetPasswordDto;
import com.kopchak.worldoftoys.exception.exception.token.InvalidConfirmationTokenException;
import com.kopchak.worldoftoys.exception.exception.token.TokenAlreadyExistException;
import com.kopchak.worldoftoys.exception.exception.user.AccountActivationException;
import com.kopchak.worldoftoys.exception.exception.user.InvalidPasswordException;
import com.kopchak.worldoftoys.exception.exception.user.UserNotFoundException;
import com.kopchak.worldoftoys.repository.token.ConfirmTokenRepository;
import com.kopchak.worldoftoys.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmationTokenServiceTest {
    @Mock
    private ConfirmTokenRepository confirmTokenRepository;
    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private ConfirmationTokenService confirmationTokenService;
    private String username;
    private ConfirmationTokenType activationTokenType;
    private AppUser user;
    private String token;
    private ConfirmationToken confirmToken;
    private ResetPasswordDto newPassword;

    @BeforeEach
    void setUp() {
        username = "user@example.com";
        activationTokenType = ConfirmationTokenType.ACTIVATION;
        user = AppUser
                .builder()
                .email(username)
                .enabled(false)
                .build();
        token = "test-token";
        confirmToken = ConfirmationToken
                .builder()
                .token(token)
                .tokenType(activationTokenType)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.MAX)
                .user(user)
                .build();
        newPassword = ResetPasswordDto.builder().password("new-password").build();
    }

    @Test
    public void createConfirmationToken_ExistingUserAndActivationTokenType_ReturnsConfirmTokenDto() throws Exception {
        when(userRepository.findByEmail(eq(username))).thenReturn(Optional.of(user));
        when(confirmTokenRepository.isNoActiveConfirmationToken(eq(username), eq(activationTokenType),
                any(LocalDateTime.class))).thenReturn(true);

        var returnedConfirmTokenDto = confirmationTokenService.createConfirmationToken(username, activationTokenType);

        assertThat(returnedConfirmTokenDto).isNotNull();
        assertThat(returnedConfirmTokenDto.token()).isNotNull();
    }

    @Test
    public void createConfirmationToken_NonExistingUser_ThrowsUserNotFoundException() {
        String userNotFoundExceptionMsg = String.format("The user with username: %s does not exist!", username);

        assertException(UserNotFoundException.class, userNotFoundExceptionMsg, () ->
                confirmationTokenService.createConfirmationToken(username, activationTokenType));
    }

    @Test
    public void createConfirmationToken_EnabledUserAndActivationTokenType_ThrowsAccountActivationException() {
        user.setEnabled(true);
        String accountActivationExceptionMsg = "The account is already activated!";

        when(userRepository.findByEmail(eq(username))).thenReturn(Optional.of(user));

        assertException(AccountActivationException.class, accountActivationExceptionMsg, () ->
                confirmationTokenService.createConfirmationToken(username, activationTokenType));
    }

    @Test
    public void createConfirmationToken_EnabledUserAndActivationTokenType_ThrowsTokenAlreadyExistException() {
        String tokenAlreadyExistExceptionMsg = "The valid confirmation token already exits!";

        when(userRepository.findByEmail(eq(username))).thenReturn(Optional.of(user));
        when(confirmTokenRepository.isNoActiveConfirmationToken(eq(username), eq(activationTokenType),
                any(LocalDateTime.class))).thenReturn(false);

        assertException(TokenAlreadyExistException.class, tokenAlreadyExistExceptionMsg, () ->
                confirmationTokenService.createConfirmationToken(username, activationTokenType));
    }

    @Test
    public void activateAccountUsingActivationToken_ExistingToken() throws InvalidConfirmationTokenException {
        when(confirmTokenRepository.findByToken(eq(token))).thenReturn(Optional.of(confirmToken));

        confirmationTokenService.activateAccountUsingActivationToken(token);

        assertThat(confirmToken.getConfirmedAt()).isNotNull();
        verify(userRepository).save(eq(user));
        verify(confirmTokenRepository).save(eq(confirmToken));
    }

    @Test
    public void activateAccountUsingActivationToken_NonExistingToken_ThrowsInvalidConfirmationTokenException() {
        String invalidConfirmTokenExceptionMsg = "The account confirmation token is invalid!";
        assertException(InvalidConfirmationTokenException.class, invalidConfirmTokenExceptionMsg,
                () -> confirmationTokenService.activateAccountUsingActivationToken(token));
    }

    @Test
    public void changePasswordUsingResetToken_ExistingToken() throws InvalidConfirmationTokenException, InvalidPasswordException {
        confirmToken.setTokenType(ConfirmationTokenType.RESET_PASSWORD);

        when(confirmTokenRepository.findByToken(eq(token))).thenReturn(Optional.of(confirmToken));

        confirmationTokenService.changePasswordUsingResetToken(token, newPassword);

        assertThat(confirmToken.getConfirmedAt()).isNotNull();
        verify(userService).changeUserPassword(eq(user), eq(newPassword.password()));
        verify(confirmTokenRepository).save(eq(confirmToken));
        verify(jwtTokenService).revokeAllUserAuthTokens(eq(user));
    }

    @Test
    public void changePasswordUsingResetToken_NonExistingToken_ThrowsInvalidConfirmationTokenException() {
        String invalidConfirmTokenExceptionMsg = "The reset password token is invalid!";

        assertException(InvalidConfirmationTokenException.class, invalidConfirmTokenExceptionMsg,
                () -> confirmationTokenService.changePasswordUsingResetToken(token, newPassword));
    }

    private void assertException(Class<? extends Exception> expectedExceptionType, String expectedMessage,
                                 Executable executable) {
        Exception exception = assertThrows(expectedExceptionType, executable);
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }
}