package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.token.AccessAndRefreshTokensDto;
import com.kopchak.worldoftoys.dto.token.AuthTokenDto;
import com.kopchak.worldoftoys.exception.exception.JwtTokenException;
import com.kopchak.worldoftoys.model.token.AuthTokenType;

import java.util.Optional;

public interface JwtTokenService {
    Optional<String> extractUsername(String token) throws JwtTokenException;

    boolean isAuthTokenValid(String token, AuthTokenType tokenType) throws JwtTokenException;

    boolean isActiveAuthTokenExists(String refreshToken, AuthTokenType tokenType) throws JwtTokenException;

    AuthTokenDto refreshAccessToken(AuthTokenDto refreshTokenDto) throws JwtTokenException;
    void revokeAllUserAuthTokens(String username);
    AccessAndRefreshTokensDto generateAuthTokens(String email);
}
