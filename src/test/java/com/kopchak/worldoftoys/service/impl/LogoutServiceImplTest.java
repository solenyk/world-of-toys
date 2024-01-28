package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.domain.token.auth.AuthenticationToken;
import com.kopchak.worldoftoys.domain.user.AppUser;
import com.kopchak.worldoftoys.repository.token.AuthTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Test
    public void logout_ValidAuthorizationHeader() {
        String authHeaderPrefix = "Bearer ";
        String validJwtToken = "valid-jwt-token";
        AppUser user = AppUser
                .builder()
                .email("test@gmail.com")
                .build();
        AuthenticationToken authToken = AuthenticationToken
                .builder()
                .token(validJwtToken)
                .user(user)
                .build();

        when(request.getHeader(eq(HttpHeaders.AUTHORIZATION))).thenReturn(authHeaderPrefix + validJwtToken);
        when(authTokenRepository.findByToken(eq(validJwtToken))).thenReturn(Optional.of(authToken));

        logoutService.logout(request, response, authentication);

        verify(authTokenRepository).findByToken(eq(validJwtToken));
        verify(authTokenRepository).save(eq(authToken));
        assertTrue(authToken.isRevoked());
        assertTrue(authToken.isExpired());
    }

    @Test
    public void logout_InvalidAuthorizationHeader() {
        logoutService.logout(request, response, authentication);

        verify(authTokenRepository, never()).findByToken(any());
        verify(authTokenRepository, never()).save(any());
    }
}