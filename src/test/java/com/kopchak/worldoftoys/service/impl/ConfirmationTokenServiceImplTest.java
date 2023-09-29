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
    }

    @Test
    void createConfirmationToken_UsernameOfExistingUserAndTokenType_ReturnsConfirmTokenDto() {
        // Arrange
        when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));

        //Act
        ConfirmTokenDto returnedConfirmTokenDto = confirmationTokenService.createConfirmationToken(username,
                activationTokenType);

        //Assert
        assertThat(returnedConfirmTokenDto).isNotNull();
        assertThat(returnedConfirmTokenDto.getToken()).isNotNull();
    }

    @Test
    void createConfirmationToken_UsernameOfNonExistingUserAndTokenType_ThrowsUserNotFoundException() {
        //Act
        ResponseStatusException exception = assertThrows(UserNotFoundException.class, () ->
                confirmationTokenService.createConfirmationToken(username, activationTokenType));

        String expectedMessage = "User with this username does not exist!";
        String actualMessage = exception.getReason();
        int expectedStatusCode = HttpStatus.NOT_FOUND.value();
        int actualStatusCode = exception.getStatusCode().value();

        //Assert
        assertEquals(expectedMessage, actualMessage);
        assertEquals(expectedStatusCode, actualStatusCode);
    }

    @Test
    void isConfirmationTokenInvalid_UsernameOfNonExistingUserAndTokenType_ReturnsFalse() {
        // Arrange
        when(confirmationTokenRepository.findByToken(token)).thenReturn(Optional.of(confirmToken));

        //Act
        boolean confirmationTokenInvalid = confirmationTokenService.isConfirmationTokenInvalid(token, activationTokenType);

        //Assert
        assertFalse(confirmationTokenInvalid);
    }

    @Test
    void isConfirmationTokenInvalid_UsernameOfNonExistingUserAndTokenType_ReturnsTrue() {
        //Act
        boolean confirmationTokenInvalid = confirmationTokenService.isConfirmationTokenInvalid(token, activationTokenType);

        //Assert
        assertTrue(confirmationTokenInvalid);
    }

    @Test
    void activateAccountUsingActivationToken_ExistingToken() {
        // Arrange
        when(confirmationTokenRepository.findByToken(token)).thenReturn(Optional.of(confirmToken));

        //Act
       confirmationTokenService.activateAccountUsingActivationToken(token);

        //Assert
        assertThat(confirmToken.getConfirmedAt()).isNotNull();
        verify(userService).activateUserAccount(user);
        verify(confirmationTokenRepository).save(confirmToken);
    }

    @Test
    void activateAccountUsingActivationToken_NonExistingToken_ThrowsInvalidConfirmationTokenException() {
        //Act
        ResponseStatusException exception = assertThrows(InvalidConfirmationTokenException.class, () ->
                confirmationTokenService.activateAccountUsingActivationToken(token));

        String expectedMessage = "This confirmation token is invalid!";
        String actualMessage = exception.getReason();
        int expectedStatusCode = HttpStatus.BAD_REQUEST.value();
        int actualStatusCode = exception.getStatusCode().value();

        //Assert
        assertEquals(expectedMessage, actualMessage);
        assertEquals(expectedStatusCode, actualStatusCode);
    }

    @Test
    void isNoActiveConfirmationToken_UsernameAndTokenType_ReturnsTrue() {
        // Arrange
        when(confirmationTokenRepository.isNoActiveConfirmationToken(eq(username), eq(activationTokenType),
                any(LocalDateTime.class))).thenReturn(true);

        //Act
        boolean isNoActiveConfirmationToken = confirmationTokenService.isNoActiveConfirmationToken(username, activationTokenType);

        //Assert
        assertTrue(isNoActiveConfirmationToken);
    }

    @Test
    void changePasswordUsingResetToken_ExistingToken() {
        // Arrange
        when(confirmationTokenRepository.findByToken(token)).thenReturn(Optional.of(confirmToken));

        //Act
        confirmationTokenService.changePasswordUsingResetToken(token, newPassword);

        //Assert
        assertThat(confirmToken.getConfirmedAt()).isNotNull();
        verify(userService).changeUserPassword(user, newPassword.getPassword());
        verify(confirmationTokenRepository).save(confirmToken);
        verify(jwtTokenService).revokeAllUserAuthTokens(username);
    }

    @Test
    void changePasswordUsingResetToken_NonExistingToken_ThrowsInvalidConfirmationTokenException() {
        //Act
        ResponseStatusException exception = assertThrows(InvalidConfirmationTokenException.class, () ->
                confirmationTokenService.changePasswordUsingResetToken(token, newPassword));

        String expectedMessage = "This confirmation token is invalid!";
        String actualMessage = exception.getReason();
        int expectedStatusCode = HttpStatus.BAD_REQUEST.value();
        int actualStatusCode = exception.getStatusCode().value();

        //Assert
        assertEquals(expectedMessage, actualMessage);
        assertEquals(expectedStatusCode, actualStatusCode);
    }
}