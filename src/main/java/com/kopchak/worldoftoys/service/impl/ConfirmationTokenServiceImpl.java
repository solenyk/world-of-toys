package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.token.ConfirmTokenDto;
import com.kopchak.worldoftoys.dto.user.ResetPasswordDto;
import com.kopchak.worldoftoys.exception.ConfirmationTokenExpiredException;
import com.kopchak.worldoftoys.exception.IncorrectPasswordException;
import com.kopchak.worldoftoys.exception.UserNotFoundException;
import com.kopchak.worldoftoys.model.token.ConfirmationTokenType;
import com.kopchak.worldoftoys.model.token.ConfirmationToken;
import com.kopchak.worldoftoys.model.user.AppUser;
import com.kopchak.worldoftoys.repository.token.ConfirmationTokenRepository;
import com.kopchak.worldoftoys.repository.user.UserRepository;
import com.kopchak.worldoftoys.service.ConfirmationTokenService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ConfirmationTokenServiceImpl implements ConfirmationTokenService {
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final int TOKEN_EXPIRATION_TIME_IN_MINUTES = 15;

    public ConfirmTokenDto createConfirmationToken(String username, ConfirmationTokenType tokenType) {
        AppUser user = userRepository.findByEmail(username).get();
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
        return new ConfirmTokenDto(confirmationToken);
    }

    public boolean isValidActivationTokenExists(String email) {
        var user = findUserByEmail(email);
        var confirmTokensList = confirmationTokenRepository
                .findAllByUserId(user.getId())
                .stream()
                .filter(confirmToken -> confirmToken.getTokenType() == ConfirmationTokenType.ACTIVATION)
                .toList();
        for (ConfirmationToken confirmationToken : confirmTokensList) {
            if (confirmationToken.getExpiresAt().isAfter(LocalDateTime.now())) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional
    public String confirmToken(String token) {
        ConfirmationToken confirmationToken = getToken(token);
        setConfirmedAt(token);
        AppUser user = confirmationToken.getUser();
        findUserByEmail(user.getEmail());
        user.setEnabled(true);
        return "Account activated! You can close this link.";
    }

    @Override
    @Transactional
    public String confirmResetToken(String token, ResetPasswordDto newPassword) {
        ConfirmationToken confirmationToken = getToken(token);
        setConfirmedAt(token);
        String email = confirmationToken.getUser().getEmail();
        var user = findUserByEmail(email);
        if (passwordEncoder.matches(newPassword.getPassword(), user.getPassword())) {
            throw new IncorrectPasswordException(HttpStatus.BAD_REQUEST, "New password matches old password!");
        }
        user.setPassword(passwordEncoder.encode(newPassword.getPassword()));
        return "Password successfully changed!";
    }

    private ConfirmationToken getToken(String token) {
        return confirmationTokenRepository.findByToken(token).orElseThrow(() ->
                new ConfirmationTokenExpiredException(HttpStatus.FORBIDDEN, "Confirmation link is expired!"));
    }

    private void setConfirmedAt(String token) {
        confirmationTokenRepository.updateConfirmedAt(token, LocalDateTime.now());
    }

    private AppUser findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new UserNotFoundException(HttpStatus.BAD_REQUEST, "Username does not exist!"));
    }

    private void saveConfirmationToken(ConfirmationToken token) {
        confirmationTokenRepository.save(token);
    }
}
