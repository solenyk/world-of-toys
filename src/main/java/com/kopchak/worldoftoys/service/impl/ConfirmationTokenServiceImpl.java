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
            String errorMsg = String.format("User with username: %s does not exist!", username);
            log.error(errorMsg);
            return new UserNotFoundException(errorMsg);
        });
        if (user.isEnabled() && tokenType.equals(ConfirmationTokenType.ACTIVATION)) {
            log.error("Account with username: {} is already activated!", username);
            throw new AccountActivationException("Account is already activated!");
        }
        if (!confirmTokenRepository.isNoActiveConfirmationToken(username, tokenType, LocalDateTime.now())) {
            log.error("Valid confirmation token for user with username: {} already exits!", username);
            throw new TokenAlreadyExistException("Valid confirmation token already exits!");
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
        log.info("Created confirmation token for user with username: {}", username);
        return new ConfirmTokenDto(confirmationToken.getToken());
    }

    @Override
    public void activateAccountUsingActivationToken(String token) throws InvalidConfirmationTokenException {
        if (isConfirmationTokenInvalid(token, ConfirmationTokenType.ACTIVATION)) {
            throw new InvalidConfirmationTokenException("Account confirmation token is invalid!");
        }
        ConfirmationToken confirmationToken = confirmTokenRepository.findByToken(token).get();
        confirmationToken.setConfirmedAt(LocalDateTime.now());
        AppUser user = confirmationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        confirmTokenRepository.save(confirmationToken);
        log.info("Activated account for user with username: {}", user.getUsername());
    }

    @Override
    public void changePasswordUsingResetToken(String token, ResetPasswordDto newPassword) throws InvalidConfirmationTokenException, InvalidPasswordException {
        if (isConfirmationTokenInvalid(token, ConfirmationTokenType.RESET_PASSWORD)) {
            throw new InvalidConfirmationTokenException("Reset password token is invalid!");
        }
        ConfirmationToken confirmationToken = confirmTokenRepository.findByToken(token).get();
        confirmationToken.setConfirmedAt(LocalDateTime.now());
        AppUser user = confirmationToken.getUser();
        userService.changeUserPassword(user, newPassword.password());
        confirmTokenRepository.save(confirmationToken);
        jwtTokenService.revokeAllUserAuthTokens(user);
        log.info("Changed password for user with username: {}", user.getUsername());
    }

    private boolean isConfirmationTokenInvalid(String token, ConfirmationTokenType tokenType) {
        Optional<ConfirmationToken> confirmationTokenOptional = confirmTokenRepository.findByToken(token);
        if (confirmationTokenOptional.isPresent()) {
            ConfirmationToken confirmToken = confirmationTokenOptional.get();
            return !confirmToken.getTokenType().equals(tokenType) ||
                    confirmToken.getConfirmedAt() != null || !confirmToken.getExpiresAt().isAfter(LocalDateTime.now());
        }
        return true;
    }
}
