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
import org.springframework.http.HttpStatus;
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
    public Optional<String> extractUsername(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return Optional.ofNullable(claims.getSubject());
        } catch (JwtException e) {
            log.error("Error extracting username from token: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean isAuthTokenValid(String token, AuthTokenType tokenType) {
        Optional<AuthenticationToken> authToken = authTokenRepository.findByToken(token);
        if (authToken.isPresent()) {
            Optional<String> username = extractUsername(token);
            return username.isPresent() && userRepository.findByEmail(username.get()).isPresent() &&
                    !isTokenExpired(token) && !authToken.get().isRevoked() &&
                    authToken.get().getTokenType().equals(tokenType);
        }
        return false;
    }

    @Override
    public AccessAndRefreshTokensDto generateAuthTokens(String email) {
        AppUser user = userRepository.findByEmail(email).orElseThrow(() ->
                new UserNotFoundException(HttpStatus.NOT_FOUND, "User with this username does not exist!"));
        String accessToken = generateJwtToken(email, AuthTokenType.ACCESS);
        String refreshToken = generateJwtToken(email, AuthTokenType.REFRESH);
        saveUserAuthToken(user, accessToken, AuthTokenType.ACCESS);
        saveUserAuthToken(user, refreshToken, AuthTokenType.REFRESH);
        log.info("Authentication tokens have been successfully generated and saved for the user: {}", email);
        return AccessAndRefreshTokensDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public boolean isActiveAuthTokenExists(String authToken, AuthTokenType tokenType) {
        Optional<String> username = extractUsername(authToken);
        return username.isPresent() && authTokenRepository.isActiveAuthTokenExists(username.get(), tokenType);
    }

    @Override
    public AuthTokenDto refreshAccessToken(AuthTokenDto refreshTokenDto) {
        String refreshToken = refreshTokenDto.getToken();
        Optional<String> username = extractUsername(refreshToken);
        if(username.isPresent()){
            String accessToken = generateJwtToken(username.get(), AuthTokenType.ACCESS);
            AppUser user = userRepository.findByEmail(username.get()).orElseThrow(() ->
                    new UserNotFoundException(HttpStatus.NOT_FOUND, "User with this username does not exist!"));
            saveUserAuthToken(user, accessToken, AuthTokenType.ACCESS);
            log.info("Authentication token have been successfully saved for the user: {}", username);
            return new AuthTokenDto(accessToken);
        }
        log.error("Fail to generate access token for the user: {} because refresh token is invalid", username);
        throw new InvalidRefreshTokenException(HttpStatus.BAD_REQUEST, "This refresh token is invalid!");
    }

    @Override
    @Transactional
    public void revokeAllUserAuthTokens(String username) {
        AppUser user = userRepository.findByEmail(username).orElseThrow(() ->
                new UserNotFoundException(HttpStatus.NOT_FOUND, "User with this username does not exist!"));
        authTokenRepository.revokeActiveUserAuthTokens(user);
        log.info("Authentication tokens have been successfully revoked for the user: {}", username);
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

    private boolean isTokenExpired(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (JwtException e) {
            log.error("Error extracting expiration date from token: {}", e.getMessage());
            return true;
        }
    }

    private Claims extractAllClaims(String token) throws JwtException{
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
