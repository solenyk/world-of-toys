package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.token.AccessAndRefreshTokensDto;
import com.kopchak.worldoftoys.dto.token.AuthTokenDto;
import com.kopchak.worldoftoys.dto.user.UserAuthDto;
import com.kopchak.worldoftoys.model.token.AuthTokenType;

import java.util.Map;

public interface JwtTokenService {
    String extractUsername(String token);

    String generateJwtToken(String username, AuthTokenType tokenType);

    String generateJwtToken(Map<String, Object> extraClaims, String username, AuthTokenType tokenType);
    boolean isAuthTokenValid(String token, AuthTokenType tokenType);

    boolean isActiveAuthTokenExists(String refreshToken, AuthTokenType tokenType);

    AuthTokenDto refreshAccessToken(AuthTokenDto refreshTokenDto);
    void revokeAllUserAuthTokens(String username);
    AccessAndRefreshTokensDto authenticateUser(String email);
}
