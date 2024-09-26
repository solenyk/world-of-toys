package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.domain.token.auth.AuthTokenType;
import com.kopchak.worldoftoys.domain.token.auth.AuthenticationToken;
import com.kopchak.worldoftoys.domain.user.AppUser;
import com.kopchak.worldoftoys.dto.token.AccessAndRefreshTokensDto;
import com.kopchak.worldoftoys.dto.token.AuthTokenDto;
import com.kopchak.worldoftoys.exception.exception.token.JwtTokenException;
import com.kopchak.worldoftoys.exception.exception.token.TokenAlreadyExistException;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class JwtTokenServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthTokenRepository authTokenRepository;

    @InjectMocks
    @Spy
    private JwtTokenService jwtTokenService;
    private final static long EXP_TOKEN_TIME_IN_SECONDS = 9579738330L;
    private final static String VALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0Ijox" +
            "Njk1NzM4MzMwLCJleHAiOjk1Nzk3MzgzMzB9.EFuKx_EUPx8pEpVGk0wIIck1nxXB8prHj7noH8Nb3QI";
    private String username;
    private String invalidToken;
    private AuthTokenType accessTokenType;
    private AppUser user;
    private AuthTokenDto validAuthTokenDto;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtTokenService, "SECRET_KEY", "testsecretkey".repeat(20));
        username = "user@example.com";
        invalidToken = "invalid-token";
        accessTokenType = AuthTokenType.ACCESS;
        user = AppUser.builder().email(username).build();
        validAuthTokenDto = AuthTokenDto.builder().token(VALID_TOKEN).build();
    }

    @Test
    public void extractUsername_ValidToken_ReturnsString() throws JwtTokenException {
        String validToken = getValidToken();

        String actualUsername = jwtTokenService.extractUsername(validToken);

        assertThat(actualUsername).isNotNull();
        assertThat(actualUsername).isEqualTo(username);
    }

    @Test
    public void extractUsername_InvalidToken_ThrowsJwtTokenException() {
        String jwtTokenExceptionMsg = "Failed to extract expiration date from token: " +
                "JWT strings must contain exactly 2 period characters. Found: 0";

        assertException(JwtTokenException.class, jwtTokenExceptionMsg,
                () -> jwtTokenService.extractUsername(invalidToken));
    }

    @Test
    public void isAuthTokenValid_ValidToken_ReturnsTrue() throws JwtTokenException {
        String validToken = getValidToken();
        AuthenticationToken validAuthToken = AuthenticationToken
                .builder()
                .token(validToken)
                .tokenType(accessTokenType)
                .revoked(false)
                .expired(false)
                .user(user)
                .build();

        when(authTokenRepository.findByToken(validToken)).thenReturn(Optional.of(validAuthToken));
        doReturn(username).when(jwtTokenService).extractUsername(validToken);
        when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));

        boolean isValid = jwtTokenService.isAuthTokenValid(validToken, accessTokenType);

        assertTrue(isValid);
    }

    @Test
    public void isAuthTokenValid_TokenThatIsNotPresent_ReturnsFalse() throws JwtTokenException {
        boolean isValid = jwtTokenService.isAuthTokenValid(invalidToken, accessTokenType);

        assertFalse(isValid);
    }

    @Test
    public void isAuthTokenValid_TokenExpiredException_ReturnsFalse() throws JwtTokenException {
        AuthenticationToken invalidAuthToken = AuthenticationToken
                .builder()
                .token(invalidToken)
                .tokenType(accessTokenType)
                .revoked(false)
                .expired(true)
                .user(user)
                .build();
        String jwtTokenExceptionMsg = "Failed to extract expiration date from token: " +
                "JWT strings must contain exactly 2 period characters. Found: 0";

        when(authTokenRepository.findByToken(eq(invalidToken))).thenReturn(Optional.of(invalidAuthToken));
        doReturn(username).when(jwtTokenService).extractUsername(eq(invalidToken));
        when(userRepository.findByEmail(eq(username))).thenReturn(Optional.of(user));

        assertException(JwtTokenException.class, jwtTokenExceptionMsg,
                () -> jwtTokenService.isAuthTokenValid(this.invalidToken, accessTokenType));
    }

    @Test
    public void generateAuthTokens_ExistingUser_ReturnsAccessAndRefreshTokensDto() {
        AccessAndRefreshTokensDto accessAndRefreshTokensDto = jwtTokenService.generateAuthTokens(user);

        assertThat(accessAndRefreshTokensDto).isNotNull();
        assertThat(accessAndRefreshTokensDto.accessToken()).isNotNull();
        assertThat(accessAndRefreshTokensDto.refreshToken()).isNotNull();
    }

    @Test
    public void refreshAccessToken_ValidAuthToken_ReturnsAuthTokenDto() throws JwtTokenException, TokenAlreadyExistException {
        AuthenticationToken authToken = AuthenticationToken.builder().tokenType(AuthTokenType.REFRESH).build();

        when(authTokenRepository.findByToken(eq(VALID_TOKEN))).thenReturn(Optional.of(authToken));
        when(userRepository.findByEmail(eq(username))).thenReturn(Optional.of(user));

        AuthTokenDto returnedAuthTokenDto = jwtTokenService.refreshAccessToken(validAuthTokenDto);

        assertThat(returnedAuthTokenDto).isNotNull();
        assertThat(returnedAuthTokenDto.token()).isNotNull();
    }

    @Test
    public void refreshAccessToken_InvalidAuthToken_ThrowsJwtTokenException() {
        var ivalidAuthTokenDto = AuthTokenDto.builder().token(invalidToken).build();
        String jwtTokenExceptionMsg = "The refresh token is invalid!";

        assertException(JwtTokenException.class, jwtTokenExceptionMsg,
                () -> jwtTokenService.refreshAccessToken(ivalidAuthTokenDto));
    }

    @Test
    public void refreshAccessToken_ActiveAuthTokenExists_ThrowsTokenAlreadyExistException() {
        AuthenticationToken authToken = AuthenticationToken.builder().tokenType(AuthTokenType.REFRESH).build();
        String tokenAlreadyExistExceptionMsg = "There is valid access token!";

        when(authTokenRepository.findByToken(eq(VALID_TOKEN))).thenReturn(Optional.of(authToken));
        when(userRepository.findByEmail(eq(username))).thenReturn(Optional.of(user));
        when(authTokenRepository.isActiveAuthTokenExists(eq(username), eq(AuthTokenType.ACCESS))).thenReturn(true);

        assertException(TokenAlreadyExistException.class, tokenAlreadyExistExceptionMsg,
                () -> jwtTokenService.refreshAccessToken(validAuthTokenDto));
    }

    @Test
    public void revokeAllUserAuthTokens_ExistingUser() {
        doNothing().when(authTokenRepository).revokeActiveUserAuthTokens(isA(AppUser.class));

        jwtTokenService.revokeAllUserAuthTokens(user);

        verify(authTokenRepository).revokeActiveUserAuthTokens(user);
    }

    private String getValidToken() {
        Instant instant = Instant.ofEpochSecond(EXP_TOKEN_TIME_IN_SECONDS);
        LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        log.warn("Token expiration date is {}", localDateTime);
        return VALID_TOKEN;
    }

    private void assertException(Class<? extends Exception> expectedExceptionType,
                                 String expectedMessage, Executable executable) {
        Exception exception = assertThrows(expectedExceptionType, executable);
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }
}