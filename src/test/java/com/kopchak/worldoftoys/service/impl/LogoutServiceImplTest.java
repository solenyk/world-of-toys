package com.kopchak.worldoftoys.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.kopchak.worldoftoys.model.token.AuthenticationToken;
import com.kopchak.worldoftoys.model.user.AppUser;
import com.kopchak.worldoftoys.repository.token.AuthTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutServiceImplTest {
    @Mock
    private AuthTokenRepository authTokenRepository;

    @InjectMocks
    private LogoutServiceImpl logoutService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    private String validJwtToken;
    private String authHeaderPrefix;
    private AuthenticationToken authToken;

    @BeforeEach
    void setUp() {
        validJwtToken = "valid-jwt-token";
        authHeaderPrefix = "Bearer ";
        AppUser user = AppUser
                .builder()
                .email("test@gmail.com")
                .build();
        authToken = AuthenticationToken
                .builder()
                .token(validJwtToken)
                .user(user)
                .build();
    }

    @Test
    void logout_ValidAuthorizationHeader() {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeaderPrefix + validJwtToken);
        when(authTokenRepository.findByToken(validJwtToken)).thenReturn(Optional.of(authToken));

        logoutService.logout(request, response, authentication);

        verify(authTokenRepository).findByToken(validJwtToken);
        verify(authTokenRepository).save(authToken);
        assertTrue(authToken.isRevoked());
        assertTrue(authToken.isExpired());
    }

    @Test
    void logout_InvalidAuthorizationHeader() {
        logoutService.logout(request, response, authentication);

        verify(authTokenRepository, never()).findByToken(any());
        verify(authTokenRepository, never()).save(any());
    }
}