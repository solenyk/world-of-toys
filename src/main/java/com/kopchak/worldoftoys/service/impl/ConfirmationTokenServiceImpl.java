package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.token.ConfirmTokenDto;
import com.kopchak.worldoftoys.dto.user.ResetPasswordDto;
import com.kopchak.worldoftoys.model.token.ConfirmationToken;
import com.kopchak.worldoftoys.model.token.ConfirmationTokenType;
import com.kopchak.worldoftoys.model.user.AppUser;
import com.kopchak.worldoftoys.repository.token.ConfirmationTokenRepository;
import com.kopchak.worldoftoys.repository.user.UserRepository;
import com.kopchak.worldoftoys.service.ConfirmationTokenService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ConfirmationTokenServiceImpl implements ConfirmationTokenService {
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final int TOKEN_EXPIRATION_TIME_IN_MINUTES = 15;

    @Override
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

    @Override
    public boolean isConfirmationTokenValid(String token, ConfirmationTokenType tokenType) {
        if (confirmationTokenRepository.findByToken(token).isPresent()) {
            ConfirmationToken confirmToken = confirmationTokenRepository.findByToken(token).get();
            return confirmToken.getTokenType().equals(tokenType) &&
                    confirmToken.getConfirmedAt() == null && confirmToken.getExpiresAt().isAfter(LocalDateTime.now());
        }
        return false;
    }

    @Override
    public void activateAccountUsingActivationToken(String token) {
        ConfirmationToken confirmationToken = confirmationTokenRepository.findByToken(token).get();
        confirmationToken.setConfirmedAt(LocalDateTime.now());
        AppUser user = confirmationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        confirmationTokenRepository.save(confirmationToken);
    }

    @Override
    public boolean isNoActiveConfirmationToken(String email, ConfirmationTokenType confirmTokenType) {
        AppUser user = userRepository.findByEmail(email).get();
        List<ConfirmationToken> confirmTokensList = confirmationTokenRepository
                .findAllByUserAndTokenType(user, confirmTokenType)
                .stream()
                .filter(confirmationToken -> confirmationToken.getExpiresAt().isAfter(LocalDateTime.now()))
                .toList();
        return confirmTokensList.isEmpty();
    }

    @Override
    public void changePasswordUsingResetToken(String token, ResetPasswordDto newPassword) {
        ConfirmationToken confirmationToken = confirmationTokenRepository.findByToken(token).get();
        confirmationToken.setConfirmedAt(LocalDateTime.now());
        AppUser user = confirmationToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword.getPassword()));
        confirmationTokenRepository.save(confirmationToken);
        userRepository.save(user);
    }
}
