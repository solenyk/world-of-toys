package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.token.AccessAndRefreshTokensDto;
import com.kopchak.worldoftoys.dto.token.AuthTokenDto;
import com.kopchak.worldoftoys.model.token.AuthTokenType;

import java.util.Map;
import java.util.Optional;

public interface JwtTokenService {
    Optional<String> extractUsername(String token);

    boolean isAuthTokenValid(String token, AuthTokenType tokenType);

    boolean isActiveAuthTokenExists(String refreshToken, AuthTokenType tokenType);

    AuthTokenDto refreshAccessToken(AuthTokenDto refreshTokenDto);
    void revokeAllUserAuthTokens(String username);
    AccessAndRefreshTokensDto generateAuthTokens(String email);
}
