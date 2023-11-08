package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.token.ConfirmTokenDto;
import com.kopchak.worldoftoys.dto.user.ResetPasswordDto;
import com.kopchak.worldoftoys.exception.InvalidConfirmationTokenException;
import com.kopchak.worldoftoys.exception.UserNotFoundException;
import com.kopchak.worldoftoys.model.token.ConfirmationToken;
import com.kopchak.worldoftoys.model.token.ConfirmationTokenType;
import com.kopchak.worldoftoys.model.user.AppUser;
import com.kopchak.worldoftoys.repository.token.ConfirmTokenRepository;
import com.kopchak.worldoftoys.repository.user.UserRepository;
import com.kopchak.worldoftoys.service.JwtTokenService;
import com.kopchak.worldoftoys.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmationTokenServiceImplTest {
    @Mock
    private ConfirmTokenRepository confirmationTokenRepository;
    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private ConfirmationTokenServiceImpl confirmationTokenService;
    private String username;
    private ConfirmationTokenType activationTokenType;
    private AppUser user;
    private String token;
    private ConfirmationToken confirmToken;
    private ResetPasswordDto newPassword;
    private String invalidConfirmationTokenExceptionMsg;

    @BeforeEach
    void setUp() {
        username = "user@example.com";
        activationTokenType = ConfirmationTokenType.ACTIVATION;
        user = AppUser.builder().email(username).build();
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
        invalidConfirmationTokenExceptionMsg = "This confirmation token is invalid!";
    }

    @Test
    public void createConfirmationToken_UsernameOfExistingUserAndTokenType_ReturnsConfirmTokenDto() {
        when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));

        ConfirmTokenDto returnedConfirmTokenDto = confirmationTokenService.createConfirmationToken(username,
                activationTokenType);

        assertThat(returnedConfirmTokenDto).isNotNull();
        assertThat(returnedConfirmTokenDto.token()).isNotNull();
    }

    @Test
    public void createConfirmationToken_UsernameOfNonExistingUserAndTokenType_ThrowsUserNotFoundException() {
        String userNotFoundExceptionMsg = "User with this username does not exist!";

        assertResponseStatusException(UserNotFoundException.class, userNotFoundExceptionMsg, HttpStatus.NOT_FOUND, () ->
                confirmationTokenService.createConfirmationToken(username, activationTokenType));
    }

    @Test
    public void isConfirmationTokenInvalid_UsernameOfNonExistingUserAndTokenType_ReturnsFalse() {
        when(confirmationTokenRepository.findByToken(token)).thenReturn(Optional.of(confirmToken));

        boolean confirmationTokenInvalid = confirmationTokenService.isConfirmationTokenInvalid(token, activationTokenType);

        assertFalse(confirmationTokenInvalid);
    }

    @Test
    public void isConfirmationTokenInvalid_UsernameOfNonExistingUserAndTokenType_ReturnsTrue() {
        boolean confirmationTokenInvalid = confirmationTokenService.isConfirmationTokenInvalid(token, activationTokenType);

        assertTrue(confirmationTokenInvalid);
    }

    @Test
    public void activateAccountUsingActivationToken_ExistingToken() {
        when(confirmationTokenRepository.findByToken(token)).thenReturn(Optional.of(confirmToken));

        confirmationTokenService.activateAccountUsingActivationToken(token);

        assertThat(confirmToken.getConfirmedAt()).isNotNull();
        verify(userService).activateUserAccount(user);
        verify(confirmationTokenRepository).save(confirmToken);
    }

    @Test
    public void activateAccountUsingActivationToken_NonExistingToken_ThrowsInvalidConfirmationTokenException() {
        assertResponseStatusException(InvalidConfirmationTokenException.class, invalidConfirmationTokenExceptionMsg,
                HttpStatus.BAD_REQUEST, () -> confirmationTokenService.activateAccountUsingActivationToken(token));
    }

    @Test
    public void isNoActiveConfirmationToken_UsernameAndTokenType_ReturnsTrue() {
        when(confirmationTokenRepository.isNoActiveConfirmationToken(eq(username), eq(activationTokenType),
                any(LocalDateTime.class))).thenReturn(true);

        boolean isNoActiveConfirmationToken = confirmationTokenService.isNoActiveConfirmationToken(username, activationTokenType);

        assertTrue(isNoActiveConfirmationToken);
    }

    @Test
    public void changePasswordUsingResetToken_ExistingToken() {
        when(confirmationTokenRepository.findByToken(token)).thenReturn(Optional.of(confirmToken));

        confirmationTokenService.changePasswordUsingResetToken(token, newPassword);

        assertThat(confirmToken.getConfirmedAt()).isNotNull();
        verify(userService).changeUserPassword(user, newPassword.password());
        verify(confirmationTokenRepository).save(confirmToken);
        verify(jwtTokenService).revokeAllUserAuthTokens(username);
    }

    @Test
    public void changePasswordUsingResetToken_NonExistingToken_ThrowsInvalidConfirmationTokenException() {
        assertResponseStatusException(InvalidConfirmationTokenException.class, invalidConfirmationTokenExceptionMsg,
                HttpStatus.BAD_REQUEST, () -> confirmationTokenService.changePasswordUsingResetToken(token, newPassword));
    }

    private void assertResponseStatusException(Class<? extends ResponseStatusException> expectedExceptionType,
                                               String expectedMessage, HttpStatus expectedHttpStatus,
                                               Executable executable) {
        ResponseStatusException exception = assertThrows(expectedExceptionType, executable);

        String actualMessage = exception.getReason();
        int expectedStatusCode = expectedHttpStatus.value();
        int actualStatusCode = exception.getStatusCode().value();

        assertEquals(expectedMessage, actualMessage);
        assertEquals(expectedStatusCode, actualStatusCode);
    }
}