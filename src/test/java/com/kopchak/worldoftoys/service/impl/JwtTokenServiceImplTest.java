package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.token.AccessAndRefreshTokensDto;
import com.kopchak.worldoftoys.dto.token.AuthTokenDto;
import com.kopchak.worldoftoys.exception.InvalidRefreshTokenException;
import com.kopchak.worldoftoys.exception.UserNotFoundException;
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
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    private String username;
    private String validToken;
    private long expTokenTimeInSeconds;
    private String invalidToken;
    private AuthTokenType accessTokenType;
    private AppUser user;
    private AuthenticationToken validAuthToken;
    private AuthenticationToken invalidAuthToken;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtTokenService, "SECRET_KEY", "testsecretkey".repeat(20));
        username = "user@example.com";
        validToken = "eyJhbGciOiJIUzI1NiJ9" +
                ".eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNjk1NzM4MzMwLCJleHAiOjk1Nzk3MzgzMzB9" +
                ".EFuKx_EUPx8pEpVGk0wIIck1nxXB8prHj7noH8Nb3QI";
        expTokenTimeInSeconds = 9579738330L;
        invalidToken = "invalid-token";
        accessTokenType = AuthTokenType.ACCESS;
        user = AppUser.builder().email(username).build();
        validAuthToken = AuthenticationToken
                .builder()
                .token(validToken)
                .tokenType(accessTokenType)
                .revoked(false)
                .expired(false)
                .user(user)
                .build();
        invalidAuthToken = AuthenticationToken
                .builder()
                .token(invalidToken)
                .tokenType(accessTokenType)
                .revoked(false)
                .expired(false)
                .user(user)
                .build();
    }

    @Test
    void extractUsername_ValidToken_ReturnsOptionalString() {
        // Arrange
        String validToken = getValidToken();

        //Act
        Optional<String> actualUsername = jwtTokenService.extractUsername(validToken);

        //Assert
        assertThat(actualUsername).isPresent();
        assertThat(actualUsername.get()).isEqualTo(username);
    }

    @Test
    void extractUsername_InvalidToken_ReturnsEmptyOptional() {
        //Act
        Optional<String> actualUsername = jwtTokenService.extractUsername(invalidToken);

        //Assert
        assertThat(actualUsername).isEmpty();
    }

    @Test
    void isAuthTokenValid_ValidToken_ReturnsTrue() {
        // Arrange
        String validToken = getValidToken();

        when(authTokenRepository.findByToken(validToken)).thenReturn(Optional.of(validAuthToken));
        doReturn(Optional.of(username)).when(jwtTokenService).extractUsername(validToken);
        when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));

        //Act
        boolean isValid = jwtTokenService.isAuthTokenValid(validToken, accessTokenType);

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
        when(authTokenRepository.findByToken(invalidToken)).thenReturn(Optional.of(invalidAuthToken));
        doReturn(Optional.of(username)).when(jwtTokenService).extractUsername(invalidToken);
        when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));

        //Act
        boolean isValid = jwtTokenService.isAuthTokenValid(invalidToken, accessTokenType);

        //Assert
        assertFalse(isValid);
    }

    @Test
    void generateAuthTokens_UsernameOfExistingUser_ReturnsAccessAndRefreshTokensDto() {
        // Arrange
        when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));

        //Act
        AccessAndRefreshTokensDto accessAndRefreshTokensDto = jwtTokenService.generateAuthTokens(username);

        //Assert
        assertThat(accessAndRefreshTokensDto).isNotNull();
        assertThat(accessAndRefreshTokensDto.getAccessToken()).isNotNull();
        assertThat(accessAndRefreshTokensDto.getRefreshToken()).isNotNull();
    }

    @Test
    void generateAuthTokens_UsernameOfNonExistingUser_ThrowsUserNotFoundException() {
        //Act
        ResponseStatusException exception = assertThrows(UserNotFoundException.class, () ->
            jwtTokenService.generateAuthTokens(username));

        String expectedMessage = "User with this username does not exist!";
        String actualMessage = exception.getReason();
        int expectedStatusCode = HttpStatus.NOT_FOUND.value();
        int actualStatusCode = exception.getStatusCode().value();

        //Assert
        assertEquals(expectedMessage, actualMessage);
        assertEquals(expectedStatusCode, actualStatusCode);
    }

    @Test
    void isActiveAuthTokenExists_ValidTokenAndAuthTokenType_ReturnsTrue() {
        // Arrange
        when(authTokenRepository.isActiveAuthTokenExists(username, accessTokenType)).thenReturn(true);

        //Act
        boolean isActiveAuthTokenExists = jwtTokenService.isActiveAuthTokenExists(validToken, accessTokenType);

        //Assert
        assertTrue(isActiveAuthTokenExists);
    }

    @Test
    void refreshAccessToken_ValidAuthTokenDtoWithExistingUser_ReturnsAuthTokenDto() {
        // Arrange
        AuthTokenDto refreshToken = AuthTokenDto
                .builder()
                .token(validToken)
                .build();
        when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));

        //Act
       AuthTokenDto returnedAuthTokenDto = jwtTokenService.refreshAccessToken(refreshToken);

        //Assert
        assertThat(returnedAuthTokenDto).isNotNull();
        assertThat(returnedAuthTokenDto.getToken()).isNotNull();
    }

    @Test
    void refreshAccessToken_ValidAuthTokenDtoWithNonExistingUser_ThrowsUserNotFoundException() {
        // Arrange
        AuthTokenDto refreshToken = AuthTokenDto
                .builder()
                .token(validToken)
                .build();

        //Act
        ResponseStatusException exception = assertThrows(UserNotFoundException.class, () ->
                jwtTokenService.refreshAccessToken(refreshToken));

        String expectedMessage = "User with this username does not exist!";
        String actualMessage = exception.getReason();
        int expectedStatusCode = HttpStatus.NOT_FOUND.value();
        int actualStatusCode = exception.getStatusCode().value();

        //Assert
        assertEquals(expectedMessage, actualMessage);
        assertEquals(expectedStatusCode, actualStatusCode);
    }

    @Test
    void refreshAccessToken_InvalidAuthTokenDto_ThrowsInvalidRefreshTokenException() {
        // Arrange
        AuthTokenDto refreshToken = AuthTokenDto
                .builder()
                .token(invalidToken)
                .build();

        //Act
        ResponseStatusException exception = assertThrows(InvalidRefreshTokenException.class, () ->
                jwtTokenService.refreshAccessToken(refreshToken));

        String expectedMessage = "This refresh token is invalid!";
        String actualMessage = exception.getReason();
        int expectedStatusCode = HttpStatus.BAD_REQUEST.value();
        int actualStatusCode = exception.getStatusCode().value();

        //Assert
        assertEquals(expectedMessage, actualMessage);
        assertEquals(expectedStatusCode, actualStatusCode);
    }

    private String getValidToken(){
        Instant instant = Instant.ofEpochSecond(expTokenTimeInSeconds);
        LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        log.warn("Token expiration date is {}", localDateTime);
        return validToken;
    }
}