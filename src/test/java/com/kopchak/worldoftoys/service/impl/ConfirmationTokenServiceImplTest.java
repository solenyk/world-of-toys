package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.token.AuthTokenDto;
import com.kopchak.worldoftoys.dto.token.ConfirmTokenDto;
import com.kopchak.worldoftoys.exception.UserNotFoundException;
import com.kopchak.worldoftoys.model.token.AuthTokenType;
import com.kopchak.worldoftoys.model.token.AuthenticationToken;
import com.kopchak.worldoftoys.model.token.ConfirmationToken;
import com.kopchak.worldoftoys.model.token.ConfirmationTokenType;
import com.kopchak.worldoftoys.model.user.AppUser;
import com.kopchak.worldoftoys.repository.token.ConfirmTokenRepository;
import com.kopchak.worldoftoys.repository.user.UserRepository;
import com.kopchak.worldoftoys.service.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
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
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ConfirmationTokenServiceImpl confirmationTokenService;
    private String username;
    private ConfirmationTokenType activationTokenType;
    private AppUser user;
    private String token;
    private ConfirmationToken confirmToken;

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
                .build();
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
}