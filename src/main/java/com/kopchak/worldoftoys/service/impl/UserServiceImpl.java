package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.token.AuthTokenDto;
import com.kopchak.worldoftoys.dto.user.UserAuthDto;
import com.kopchak.worldoftoys.dto.user.UserRegistrationDto;
import com.kopchak.worldoftoys.model.token.AuthTokenType;
import com.kopchak.worldoftoys.model.token.AuthenticationToken;
import com.kopchak.worldoftoys.model.token.ConfirmationToken;
import com.kopchak.worldoftoys.model.user.AppUser;
import com.kopchak.worldoftoys.model.user.Role;
import com.kopchak.worldoftoys.repository.token.AuthTokenRepository;
import com.kopchak.worldoftoys.repository.token.ConfirmationTokenRepository;
import com.kopchak.worldoftoys.repository.user.UserRepository;
import com.kopchak.worldoftoys.service.JwtTokenService;
import com.kopchak.worldoftoys.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final AuthenticationManager authenticationManager;

    @Override
    public void registerUser(UserRegistrationDto userRegistrationDto) {
        AppUser user = AppUser.builder()
                .firstname(userRegistrationDto.getFirstname())
                .lastname(userRegistrationDto.getLastname())
                .email(userRegistrationDto.getEmail())
                .password(passwordEncoder.encode(userRegistrationDto.getPassword()))
                .role(Role.ROLE_USER)
                .enabled(false)
                .locked(false)
                .build();
        userRepository.save(user);
    }

    @Override
    public boolean isUserRegistered(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public boolean isUserActivated(String email) {
        AppUser user = userRepository.findByEmail(email).get();
        return user.getEnabled();
    }

    @Override
    public boolean isNewPasswordMatchOldPassword(String resetPasswordToken, String newPassword) {
        ConfirmationToken confirmationToken = confirmationTokenRepository.findByToken(resetPasswordToken).get();
        AppUser user = confirmationToken.getUser();
        return passwordEncoder.matches(newPassword, user.getPassword());
    }

    @Override
    public boolean isPasswordsMatch(String email, String password) {
        AppUser user = userRepository.findByEmail(email).get();
        return passwordEncoder.matches(password, user.getPassword());
    }

    @Override
    public AuthTokenDto authenticate(UserAuthDto userAuthDto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userAuthDto.getEmail(),
                        userAuthDto.getPassword()
                )
        );
        AppUser user = userRepository.findByEmail(userAuthDto.getEmail()).get();
        String jwtToken = jwtTokenService.generateAccessToken(user);
        revokeAllUserTokens(user);
        saveUserAuthToken(user, jwtToken);
        return AuthTokenDto.builder()
                .token(jwtToken)
                .build();
    }

    private void revokeAllUserTokens(AppUser user) {
        List<AuthenticationToken> userAuthTokens =
                authTokenRepository.findAllByUser(user)
                        .stream()
                        .peek(token -> {
                            token.setExpired(true);
                            token.setRevoked(true);
                        }).collect(Collectors.toList());
        authTokenRepository.saveAll(userAuthTokens);
    }

    private void saveUserAuthToken(AppUser user, String jwtToken) {
        var token = AuthenticationToken
                .builder()
                .token(jwtToken)
                .user(user)
                .tokenType(AuthTokenType.ACCESS)
                .expired(false)
                .revoked(false)
                .build();
        authTokenRepository.save(token);
    }
}
