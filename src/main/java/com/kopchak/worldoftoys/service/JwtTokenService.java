package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.model.token.AuthTokenType;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;
import java.util.function.Function;

public interface JwtTokenService {
    String extractUsername(String token);

    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

    String generateJwtToken(String username, AuthTokenType tokenType);

    String generateJwtToken(Map<String, Object> extraClaims, String username, AuthTokenType tokenType);
    boolean isTokenValid(String token, UserDetails userDetails);
}
