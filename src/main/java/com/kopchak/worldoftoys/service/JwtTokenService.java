package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.token.AuthTokenDto;
import com.kopchak.worldoftoys.model.token.AuthTokenType;
import io.jsonwebtoken.Claims;

import java.util.Map;
import java.util.function.Function;

public interface JwtTokenService {
    String extractUsername(String token);

    String generateJwtToken(String username, AuthTokenType tokenType);

    String generateJwtToken(Map<String, Object> extraClaims, String username, AuthTokenType tokenType);
    boolean isAuthTokenValid(String token, AuthTokenType tokenType);

    boolean isActiveAccessTokenExists(String refreshToken);

    AuthTokenDto refreshAccessToken(AuthTokenDto refreshTokenDto);
    void revokeAllUserAuthTokens(String username);
}
