package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.repository.token.AuthTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutService implements LogoutHandler {
    private final AuthTokenRepository authTokenRepository;
    private static final String BEARER = "Bearer ";

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER)) {
            log.info("The authorization header is null or doesn't start with 'BEARER'");
            return;
        }
        final String jwt = authHeader.substring(BEARER.length());
        var storedToken = authTokenRepository.findByToken(jwt).orElse(null);
        if (storedToken != null) {
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            authTokenRepository.save(storedToken);
            SecurityContextHolder.clearContext();
            log.info("The user with the username: {} has logged out", storedToken.getUser().getUsername());
        }
    }
}
