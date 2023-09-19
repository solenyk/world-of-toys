package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.model.token.AuthTokenType;
import com.kopchak.worldoftoys.service.JwtTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtTokenServiceImpl implements JwtTokenService {
    @Value(value = "${security.jwt.secret}")
    private String SECRET_KEY;
    private static final int ACCESS_TOKEN_EXPIRATION_TIME_IN_MILLIS = 1000 * 60 * 24 * 14;
    private static final int REFRESH_TOKEN_EXPIRATION_TIME_IN_MILLIS = 1000 * 60 * 24 * 14;

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
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
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
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
}
