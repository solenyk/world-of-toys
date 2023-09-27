package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.model.token.AuthTokenType;
import com.kopchak.worldoftoys.model.token.AuthenticationToken;
import com.kopchak.worldoftoys.model.user.AppUser;
import com.kopchak.worldoftoys.repository.token.AuthTokenRepository;
import com.kopchak.worldoftoys.repository.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Slf4j
class JwtTokenServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthTokenRepository authTokenRepository;

    @InjectMocks
    @Spy
    private JwtTokenServiceImpl jwtTokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtTokenService, "SECRET_KEY", "testsecretkey".repeat(20));
    }

    @Test
    void extractUsername_ValidToken_ReturnsOptionalString() {
        // Arrange
        String expectedUsername = "user@example.com";
        String token = "eyJhbGciOiJIUzI1NiJ9" +
                ".eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNjk1NzM4MzMwLCJleHAiOjk1Nzk3MzgzMzB9" +
                ".EFuKx_EUPx8pEpVGk0wIIck1nxXB8prHj7noH8Nb3QI";

        long expTokenTimeInSeconds = 9579738330L;
        Instant instant = Instant.ofEpochSecond(expTokenTimeInSeconds);
        LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        log.warn("Token expiration date is {}", localDateTime);


        //Act
        Optional<String> actualUsername = jwtTokenService.extractUsername(token);

        //Assert
        assertThat(actualUsername).isPresent();
        assertThat(actualUsername.get()).isEqualTo(expectedUsername);
    }

    @Test
    void extractUsername_InvalidToken_ReturnsEmptyOptional() {
        // Arrange
        String token = "invalid-token";

        //Act
        Optional<String> actualUsername = jwtTokenService.extractUsername(token);

        //Assert
        assertThat(actualUsername).isEmpty();
    }

    @Test
    void isAuthTokenValid_ValidToken_ReturnsTrue() {
        // Arrange
        String username = "user@example.com";
        AuthTokenType tokenType = AuthTokenType.ACCESS;
        String token = "eyJhbGciOiJIUzI1NiJ9" +
                ".eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNjk1NzM4MzMwLCJleHAiOjk1Nzk3MzgzMzB9" +
                ".EFuKx_EUPx8pEpVGk0wIIck1nxXB8prHj7noH8Nb3QI";

        long expTokenTimeInSeconds = 9579738330L;
        Instant instant = Instant.ofEpochSecond(expTokenTimeInSeconds);
        LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        log.warn("Token expiration date is {}", localDateTime);

        AppUser user = AppUser
                .builder()
                .email(username)
                .build();

        AuthenticationToken authToken = AuthenticationToken
                .builder()
                .token(token)
                .tokenType(tokenType)
                .revoked(false)
                .expired(false)
                .user(user)
                .build();

        when(authTokenRepository.findByToken(token)).thenReturn(Optional.of(authToken));
        doReturn(Optional.of(username)).when(jwtTokenService).extractUsername(token);
        when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));

        //Act
        boolean isValid = jwtTokenService.isAuthTokenValid(token, tokenType);

        //Assert
        assertTrue(isValid);
    }

    @Test
    void isAuthTokenValid_TokenThatIsNotPresent_ReturnsFalse() {
        // Arrange
        AuthTokenType tokenType = AuthTokenType.ACCESS;
        String token = "not-present-token";

        //Act
        boolean isValid = jwtTokenService.isAuthTokenValid(token, tokenType);

        //Assert
        assertFalse(isValid);
    }

    @Test
    void isAuthTokenValid_InvalidToken_ReturnsFalse() {
        // Arrange
        String username = "user@example.com";
        AuthTokenType tokenType = AuthTokenType.ACCESS;
        String token = "invalid-token";

        AppUser user = AppUser
                .builder()
                .email(username)
                .build();

        AuthenticationToken authToken = AuthenticationToken
                .builder()
                .token(token)
                .tokenType(tokenType)
                .revoked(false)
                .expired(false)
                .user(user)
                .build();

        when(authTokenRepository.findByToken(token)).thenReturn(Optional.of(authToken));
        doReturn(Optional.of(username)).when(jwtTokenService).extractUsername(token);
        when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));

        //Act
        boolean isValid = jwtTokenService.isAuthTokenValid(token, tokenType);

        //Assert
        assertFalse(isValid);
    }
}