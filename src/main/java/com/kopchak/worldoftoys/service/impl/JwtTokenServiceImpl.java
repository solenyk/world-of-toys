package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.token.AccessAndRefreshTokensDto;
import com.kopchak.worldoftoys.dto.token.AuthTokenDto;
import com.kopchak.worldoftoys.model.token.AuthTokenType;
import com.kopchak.worldoftoys.model.token.AuthenticationToken;
import com.kopchak.worldoftoys.model.user.AppUser;
import com.kopchak.worldoftoys.repository.token.AuthTokenRepository;
import com.kopchak.worldoftoys.repository.user.UserRepository;
import com.kopchak.worldoftoys.service.JwtTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class JwtTokenServiceImpl implements JwtTokenService {
    @Value(value = "${security.jwt.secret}")
    private String SECRET_KEY;
    private static final int ACCESS_TOKEN_EXPIRATION_TIME_IN_MILLIS = 1000 * 60 * 24;
    private static final int REFRESH_TOKEN_EXPIRATION_TIME_IN_MILLIS = 1000 * 60 * 24 * 14;
    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;

    private static final Logger LOG =   LoggerFactory.getLogger(JwtTokenServiceImpl.class);

    @Override
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    @Override
    public String generateJwtToken(String username, AuthTokenType tokenType) {
        return generateJwtToken(new HashMap<>(), username, tokenType);
    }

    @Override
    public String generateJwtToken(Map<String, Object> extraClaims, String username,
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

    @Override
    public boolean isAuthTokenValid(String token, AuthTokenType tokenType) {
        Optional<AuthenticationToken> authToken = authTokenRepository.findByToken(token);
        if (authToken.isPresent()) {
            String username = extractUsername(token);
            return username != null && userRepository.findByEmail(username).isPresent() && !isTokenExpired(token) &&
                    !authToken.get().isRevoked() && authToken.get().getTokenType().equals(tokenType);
        }
        return false;
    }
    @Override
    public boolean isActiveAuthTokenExists(String authToken, AuthTokenType tokenType) {
        String username = extractUsername(authToken);
        return authTokenRepository.isActiveAuthTokenExists(username, tokenType);
    }

    @Override
    public AuthTokenDto refreshAccessToken(AuthTokenDto refreshTokenDto) {
        String refreshToken = refreshTokenDto.getToken();
        String username = extractUsername(refreshToken);
        String accessToken = generateJwtToken(username, AuthTokenType.ACCESS);
        AppUser user = userRepository.findByEmail(username).get();
        saveUserAuthToken(user, accessToken, AuthTokenType.ACCESS);
        return new AuthTokenDto(accessToken);
    }

    @Override
    @Transactional
    public void revokeAllUserAuthTokens(String username) {
        AppUser user = userRepository.findByEmail(username).get();
        authTokenRepository.revokeActiveUserAuthTokens(user);
    }

    @Override
    public AccessAndRefreshTokensDto authenticateUser(String email) {
        AppUser user = userRepository.findByEmail(email).get();
        String accessToken = generateJwtToken(email, AuthTokenType.ACCESS);
        String refreshToken = generateJwtToken(email, AuthTokenType.REFRESH);
        saveUserAuthToken(user, accessToken, AuthTokenType.ACCESS);
        saveUserAuthToken(user, refreshToken, AuthTokenType.REFRESH);
        return AccessAndRefreshTokensDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
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
