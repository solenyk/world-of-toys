package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.token.AccessAndRefreshTokensDto;
import com.kopchak.worldoftoys.dto.token.AuthTokenDto;
import com.kopchak.worldoftoys.exception.JwtTokenException;
import com.kopchak.worldoftoys.exception.TokenAlreadyExistException;
import com.kopchak.worldoftoys.domain.token.AuthTokenType;
import com.kopchak.worldoftoys.domain.user.AppUser;

public interface JwtTokenService {
    String extractUsername(String token) throws JwtTokenException;

    boolean isAuthTokenValid(String token, AuthTokenType tokenType) throws JwtTokenException;

    AuthTokenDto refreshAccessToken(AuthTokenDto refreshTokenDto) throws JwtTokenException, TokenAlreadyExistException;

    void revokeAllUserAuthTokens(AppUser user);

    AccessAndRefreshTokensDto generateAuthTokens(AppUser user);
}
