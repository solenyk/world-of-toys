package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.token.ConfirmTokenDto;
import com.kopchak.worldoftoys.dto.user.ResetPasswordDto;
import com.kopchak.worldoftoys.exception.exception.*;
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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class ConfirmationTokenServiceImpl implements ConfirmationTokenService {
    private final ConfirmTokenRepository confirmTokenRepository;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final UserService userService;
    private static final int TOKEN_EXPIRATION_TIME_IN_MINUTES = 15;

    @Override
    public ConfirmTokenDto createConfirmationToken(String username, ConfirmationTokenType tokenType)
            throws UserNotFoundException, TokenAlreadyExistException, AccountActivationException {
        AppUser user = userRepository.findByEmail(username).orElseThrow(() -> {
            log.error("User with username: {} does not exist!", username);
            return new UserNotFoundException(String.format("User with username: %s does not exist!", username));
        });
        if (user.isEnabled() && tokenType.equals(ConfirmationTokenType.ACTIVATION)) {
            log.error("Account with username: {} is already activated!", username);
            throw new AccountActivationException(String.format("Account with username: %s is already activated!",
                    username));
        }
        if (!isNoActiveConfirmationToken(username, tokenType)) {
            throw new TokenAlreadyExistException(
                    String.format("Valid confirmation token for user wih username: %s already exits!", username));
        }
        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = ConfirmationToken
                .builder()
                .token(token)
                .tokenType(tokenType)
                .user(user)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(TOKEN_EXPIRATION_TIME_IN_MINUTES))
                .build();
        confirmTokenRepository.save(confirmationToken);
        log.info("Created confirmation token for user: {}", username);
        return new ConfirmTokenDto(confirmationToken.getToken());
    }

    @Override
    public boolean isConfirmationTokenInvalid(String token, ConfirmationTokenType tokenType) {
        Optional<ConfirmationToken> confirmationTokenOptional = confirmTokenRepository.findByToken(token);
        if (confirmationTokenOptional.isPresent()) {
            ConfirmationToken confirmToken = confirmationTokenOptional.get();
            return !confirmToken.getTokenType().equals(tokenType) ||
                    confirmToken.getConfirmedAt() != null || !confirmToken.getExpiresAt().isAfter(LocalDateTime.now());
        }
        return true;
    }

    @Override
    public void activateAccountUsingActivationToken(String token) throws InvalidConfirmationTokenException {
        if (isConfirmationTokenInvalid(token, ConfirmationTokenType.ACTIVATION)) {
            log.error("Account confirmation token: {} is invalid!", token);
            throw new InvalidConfirmationTokenException(String.format("Account confirmation token: %s is invalid!", token));
        }
        ConfirmationToken confirmationToken = confirmTokenRepository.findByToken(token).get();
        confirmationToken.setConfirmedAt(LocalDateTime.now());
        AppUser user = confirmationToken.getUser();
        userService.activateUserAccount(user);
        confirmTokenRepository.save(confirmationToken);
        log.info("Activated account for user with email: {}", user.getUsername());
    }

    @Override
    public boolean isNoActiveConfirmationToken(String email, ConfirmationTokenType confirmTokenType) {
        return confirmTokenRepository.isNoActiveConfirmationToken(email, confirmTokenType, LocalDateTime.now());
    }

    @Override
    public void changePasswordUsingResetToken(String token, ResetPasswordDto newPassword) throws InvalidConfirmationTokenException, InvalidPasswordException {
        if (isConfirmationTokenInvalid(token, ConfirmationTokenType.RESET_PASSWORD)) {
            log.error("Reset password token: {} is invalid!", token);
            throw new InvalidConfirmationTokenException(String.format("Reset password token: %s is invalid!", token));
        }
        ConfirmationToken confirmationToken = confirmTokenRepository.findByToken(token).get();
        confirmationToken.setConfirmedAt(LocalDateTime.now());
        AppUser user = confirmationToken.getUser();
        userService.changeUserPassword(user, newPassword.password());
        confirmTokenRepository.save(confirmationToken);
        jwtTokenService.revokeAllUserAuthTokens(user);
        log.info("Changed password for user: {}", user.getUsername());
    }
}
