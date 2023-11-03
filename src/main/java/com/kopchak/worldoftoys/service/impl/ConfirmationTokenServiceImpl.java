package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.token.ConfirmTokenDto;
import com.kopchak.worldoftoys.dto.user.ResetPasswordDto;
import com.kopchak.worldoftoys.exception.InvalidConfirmationTokenException;
import com.kopchak.worldoftoys.exception.UserNotFoundException;
import com.kopchak.worldoftoys.model.token.ConfirmationToken;
import com.kopchak.worldoftoys.model.token.ConfirmationTokenType;
import com.kopchak.worldoftoys.model.user.AppUser;
import com.kopchak.worldoftoys.repository.token.ConfirmTokenRepository;
import com.kopchak.worldoftoys.repository.user.UserRepository;
import com.kopchak.worldoftoys.service.ConfirmationTokenService;
import com.kopchak.worldoftoys.service.JwtTokenService;
import com.kopchak.worldoftoys.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class ConfirmationTokenServiceImpl implements ConfirmationTokenService {
    private final ConfirmTokenRepository confirmationTokenRepository;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final UserService userService;
    private static final int TOKEN_EXPIRATION_TIME_IN_MINUTES = 15;

    @Override
    public ConfirmTokenDto createConfirmationToken(String username, ConfirmationTokenType tokenType) {
        AppUser user = userRepository.findByEmail(username).orElseThrow(() ->
                new UserNotFoundException(HttpStatus.NOT_FOUND, "User with this username does not exist!"));
        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = ConfirmationToken
                .builder()
                .token(token)
                .tokenType(tokenType)
                .user(user)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(TOKEN_EXPIRATION_TIME_IN_MINUTES))
                .build();
        confirmationTokenRepository.save(confirmationToken);
        log.info("Created confirmation token for user: {}", username);
        return new ConfirmTokenDto(confirmationToken);
    }

    @Override
    public boolean isConfirmationTokenInvalid(String token, ConfirmationTokenType tokenType) {
        Optional<ConfirmationToken> confirmationTokenOptional = confirmationTokenRepository.findByToken(token);
        if (confirmationTokenOptional.isPresent()) {
            ConfirmationToken confirmToken = confirmationTokenOptional.get();
            return !confirmToken.getTokenType().equals(tokenType) ||
                    confirmToken.getConfirmedAt() != null || !confirmToken.getExpiresAt().isAfter(LocalDateTime.now());
        }
        return true;
    }

    @Override
    public void activateAccountUsingActivationToken(String token) {
        ConfirmationToken confirmationToken = confirmationTokenRepository.findByToken(token).orElseThrow(() ->
                new InvalidConfirmationTokenException(HttpStatus.BAD_REQUEST, "This confirmation token is invalid!"));
        confirmationToken.setConfirmedAt(LocalDateTime.now());
        AppUser user = confirmationToken.getUser();
        userService.activateUserAccount(user);
        confirmationTokenRepository.save(confirmationToken);
        log.info("Activated account for user: {}", user.getUsername());
    }

    @Override
    public boolean isNoActiveConfirmationToken(String email, ConfirmationTokenType confirmTokenType) {
        return confirmationTokenRepository.isNoActiveConfirmationToken(email, confirmTokenType, LocalDateTime.now());
    }

    @Override
    public void changePasswordUsingResetToken(String token, ResetPasswordDto newPassword) {
        ConfirmationToken confirmationToken = confirmationTokenRepository.findByToken(token).orElseThrow(() ->
                new InvalidConfirmationTokenException(HttpStatus.BAD_REQUEST, "This confirmation token is invalid!"));
        confirmationToken.setConfirmedAt(LocalDateTime.now());
        AppUser user = confirmationToken.getUser();
        userService.changeUserPassword(user, newPassword.getPassword());
        confirmationTokenRepository.save(confirmationToken);
        jwtTokenService.revokeAllUserAuthTokens(user.getUsername());
        log.info("Changed password for user: {}", user.getUsername());
    }
}
