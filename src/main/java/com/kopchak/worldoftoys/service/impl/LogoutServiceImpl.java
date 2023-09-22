package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.repository.token.AuthTokenRepository;
import com.kopchak.worldoftoys.service.LogoutService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutServiceImpl implements LogoutService {
    private final AuthTokenRepository authTokenRepository;
    private static final String BEARER = "Bearer ";

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER)) {
            return;
        }
        final String jwt = authHeader.substring(BEARER.length());
        var storedToken = authTokenRepository.findByToken(jwt).orElse(null);
        if (storedToken != null) {
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            authTokenRepository.save(storedToken);
            SecurityContextHolder.clearContext();
        }
    }
}
