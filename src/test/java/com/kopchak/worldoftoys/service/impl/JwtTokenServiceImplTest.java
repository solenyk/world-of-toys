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
import org.junit.jupiter.api.function.Executable;
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
import static org.mockito.Mockito.*;

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
    private AuthTokenDto validAuthTokenDto;
    private AuthTokenDto invalidAuthTokenDto;
    private String userNotFoundExceptionMsg;
    private String invalidRefreshTokenExceptionMsg;

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
        validAuthTokenDto = AuthTokenDto.builder().token(validToken).build();
        invalidAuthTokenDto = AuthTokenDto.builder().token(invalidToken).build();
        userNotFoundExceptionMsg = "User with this username does not exist!";
        invalidRefreshTokenExceptionMsg = "This refresh token is invalid!";
    }

    @Test
    void extractUsername_ValidToken_ReturnsOptionalString() {
        String validToken = getValidToken();

        Optional<String> actualUsername = jwtTokenService.extractUsername(validToken);

        assertThat(actualUsername).isPresent();
        assertThat(actualUsername.get()).isEqualTo(username);
    }

    @Test
    void extractUsername_InvalidToken_ReturnsEmptyOptional() {
        Optional<String> actualUsername = jwtTokenService.extractUsername(invalidToken);

        assertThat(actualUsername).isEmpty();
    }

    @Test
    void isAuthTokenValid_ValidToken_ReturnsTrue() {
        String validToken = getValidToken();

        when(authTokenRepository.findByToken(validToken)).thenReturn(Optional.of(validAuthToken));
        doReturn(Optional.of(username)).when(jwtTokenService).extractUsername(validToken);
        when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));

        boolean isValid = jwtTokenService.isAuthTokenValid(validToken, accessTokenType);

        assertTrue(isValid);
    }

    @Test
    void isAuthTokenValid_TokenThatIsNotPresent_ReturnsFalse() {
        boolean isValid = jwtTokenService.isAuthTokenValid(invalidToken, accessTokenType);

        assertFalse(isValid);
    }

    @Test
    void isAuthTokenValid_InvalidToken_ReturnsFalse() {
        when(authTokenRepository.findByToken(invalidToken)).thenReturn(Optional.of(invalidAuthToken));
        doReturn(Optional.of(username)).when(jwtTokenService).extractUsername(invalidToken);
        when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));

        boolean isValid = jwtTokenService.isAuthTokenValid(invalidToken, accessTokenType);

        assertFalse(isValid);
    }

    @Test
    void generateAuthTokens_UsernameOfExistingUser_ReturnsAccessAndRefreshTokensDto() {
        when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));

        AccessAndRefreshTokensDto accessAndRefreshTokensDto = jwtTokenService.generateAuthTokens(username);

        assertThat(accessAndRefreshTokensDto).isNotNull();
        assertThat(accessAndRefreshTokensDto.getAccessToken()).isNotNull();
        assertThat(accessAndRefreshTokensDto.getRefreshToken()).isNotNull();
    }

    @Test
    void generateAuthTokens_UsernameOfNonExistingUser_ThrowsUserNotFoundException() {
        assertResponseStatusException(UserNotFoundException.class, userNotFoundExceptionMsg, HttpStatus.NOT_FOUND, () ->
                jwtTokenService.generateAuthTokens(username));
    }

    @Test
    void isActiveAuthTokenExists_ValidTokenAndAuthTokenType_ReturnsTrue() {
        when(authTokenRepository.isActiveAuthTokenExists(username, accessTokenType)).thenReturn(true);

        boolean isActiveAuthTokenExists = jwtTokenService.isActiveAuthTokenExists(validToken, accessTokenType);

        assertTrue(isActiveAuthTokenExists);
    }

    @Test
    void refreshAccessToken_ValidAuthTokenDtoWithExistingUser_ReturnsAuthTokenDto() {
        when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));

        AuthTokenDto returnedAuthTokenDto = jwtTokenService.refreshAccessToken(validAuthTokenDto);

        assertThat(returnedAuthTokenDto).isNotNull();
        assertThat(returnedAuthTokenDto.getToken()).isNotNull();
    }

    @Test
    void refreshAccessToken_ValidAuthTokenDtoWithNonExistingUser_ThrowsUserNotFoundException() {
        assertResponseStatusException(UserNotFoundException.class, userNotFoundExceptionMsg, HttpStatus.NOT_FOUND,() ->
                jwtTokenService.refreshAccessToken(validAuthTokenDto));
    }

    @Test
    void refreshAccessToken_InvalidAuthTokenDto_ThrowsInvalidRefreshTokenException() {
        assertResponseStatusException(InvalidRefreshTokenException.class, invalidRefreshTokenExceptionMsg,
                HttpStatus.BAD_REQUEST, () -> jwtTokenService.refreshAccessToken(invalidAuthTokenDto));
    }

    @Test
    void revokeAllUserAuthTokens_UsernameOfExistingUser() {
        when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));
        doNothing().when(authTokenRepository).revokeActiveUserAuthTokens(isA(AppUser.class));

        jwtTokenService.revokeAllUserAuthTokens(username);

        verify(authTokenRepository, times(1)).revokeActiveUserAuthTokens(user);
    }

    @Test
    void revokeAllUserAuthTokens_UsernameOfNonExistingUser_ThrowsUserNotFoundException() {
        assertResponseStatusException(UserNotFoundException.class, userNotFoundExceptionMsg, HttpStatus.NOT_FOUND, () ->
                jwtTokenService.revokeAllUserAuthTokens(username));
    }

    private String getValidToken() {
        Instant instant = Instant.ofEpochSecond(expTokenTimeInSeconds);
        LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        log.warn("Token expiration date is {}", localDateTime);
        return validToken;
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