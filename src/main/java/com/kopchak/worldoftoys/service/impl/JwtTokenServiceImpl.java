package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.token.AccessAndRefreshTokensDto;
import com.kopchak.worldoftoys.dto.token.AuthTokenDto;
import com.kopchak.worldoftoys.exception.exception.token.JwtTokenException;
import com.kopchak.worldoftoys.exception.exception.token.TokenAlreadyExistException;
import com.kopchak.worldoftoys.domain.token.AuthTokenType;
import com.kopchak.worldoftoys.domain.token.AuthenticationToken;
import com.kopchak.worldoftoys.domain.user.AppUser;
import com.kopchak.worldoftoys.repository.token.AuthTokenRepository;
import com.kopchak.worldoftoys.repository.user.UserRepository;
import com.kopchak.worldoftoys.service.JwtTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtTokenServiceImpl implements JwtTokenService {
    @Value(value = "${security.jwt.secret}")
    private String SECRET_KEY;
    private static final int ACCESS_TOKEN_EXPIRATION_TIME_IN_MILLIS = 1000 * 60 * 60 * 24;

    private static final int REFRESH_TOKEN_EXPIRATION_TIME_IN_MILLIS = 1000 * 60 * 60 * 24 * 14;
    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;

    @Override
    public String extractUsername(String token) throws JwtTokenException {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getSubject();
        } catch (JwtException e) {
            String errorMsg = String.format("Failed to extract expiration date from token: %s", e.getMessage());
            log.error(errorMsg);
            throw new JwtTokenException(errorMsg);
        }
    }

    @Override
    public boolean isAuthTokenValid(String token, AuthTokenType tokenType) throws JwtTokenException {
        Optional<AuthenticationToken> authToken = authTokenRepository.findByToken(token);
        if (authToken.isPresent()) {
            String username = extractUsername(token);
            return username != null && userRepository.findByEmail(username).isPresent() &&
                    !isTokenExpired(token) && !authToken.get().isRevoked() &&
                    authToken.get().getTokenType().equals(tokenType);
        }
        return false;
    }

    @Override
    public AccessAndRefreshTokensDto generateAuthTokens(AppUser user) {
        String email = user.getEmail();
        String accessToken = generateJwtToken(email, AuthTokenType.ACCESS);
        String refreshToken = generateJwtToken(email, AuthTokenType.REFRESH);
        saveUserAuthToken(user, accessToken, AuthTokenType.ACCESS);
        saveUserAuthToken(user, refreshToken, AuthTokenType.REFRESH);
        log.info("Authentication tokens have been successfully generated and " +
                "saved for the user with the username: {}", email);
        return AccessAndRefreshTokensDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthTokenDto refreshAccessToken(AuthTokenDto refreshTokenDto) throws JwtTokenException, TokenAlreadyExistException {
        String refreshToken = refreshTokenDto.token();
        if (!isAuthTokenValid(refreshToken, AuthTokenType.REFRESH)) {
            throw new JwtTokenException("The refresh token is invalid!");
        }
        String username = extractUsername(refreshToken);
        if (authTokenRepository.isActiveAuthTokenExists(username, AuthTokenType.ACCESS)) {
            log.error("There is valid access token for the user with the username: {}!", username);
            throw new TokenAlreadyExistException("There is valid access token!");
        }
        AppUser user = userRepository.findByEmail(username).get();
        String accessToken = generateJwtToken(username, AuthTokenType.ACCESS);
        saveUserAuthToken(user, accessToken, AuthTokenType.ACCESS);
        log.info("The authentication token has been successfully saved for the user with the username: {}", username);
        return new AuthTokenDto(accessToken);
    }

    @Override
    @Transactional
    public void revokeAllUserAuthTokens(AppUser user) {
        authTokenRepository.revokeActiveUserAuthTokens(user);
        log.info("Authentication tokens have been successfully revoked for the user with " +
                "the username: {}", user.getEmail());
    }

    private String generateJwtToken(String username, AuthTokenType tokenType) {
        return generateJwtToken(new HashMap<>(), username, tokenType);
    }

    private String generateJwtToken(Map<String, Object> extraClaims, String username,
                                    AuthTokenType tokenType) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() +
                        (tokenType.equals(AuthTokenType.ACCESS) ? ACCESS_TOKEN_EXPIRATION_TIME_IN_MILLIS
                                : REFRESH_TOKEN_EXPIRATION_TIME_IN_MILLIS)))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private boolean isTokenExpired(String token) throws JwtTokenException {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (JwtException e) {
            String errorMsg = String.format("Failed to extract expiration date from token: %s", e.getMessage());
            log.error(errorMsg);
            throw new JwtTokenException(errorMsg);
        }
    }

    private Claims extractAllClaims(String token) throws JwtException {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private void saveUserAuthToken(AppUser user, String jwtToken, AuthTokenType tokenType) {
        var token = AuthenticationToken
                .builder()
                .token(jwtToken)
                .user(user)
                .tokenType(tokenType)
                .expired(false)
                .revoked(false)
                .build();
        authTokenRepository.save(token);
    }
}
