package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.token.AccessAndRefreshTokensDto;
import com.kopchak.worldoftoys.dto.user.UserAuthDto;
import com.kopchak.worldoftoys.dto.user.UserRegistrationDto;
import com.kopchak.worldoftoys.exception.exception.user.AccountActivationException;
import com.kopchak.worldoftoys.exception.exception.user.InvalidPasswordException;
import com.kopchak.worldoftoys.exception.exception.user.UserNotFoundException;
import com.kopchak.worldoftoys.exception.exception.user.UsernameAlreadyExistException;
import com.kopchak.worldoftoys.domain.user.AppUser;
import com.kopchak.worldoftoys.domain.user.Role;
import com.kopchak.worldoftoys.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final AuthenticationManager authenticationManager;

    public void registerUser(UserRegistrationDto userRegistrationDto) {
        String email = userRegistrationDto.email();
        if (userRepository.findByEmail(email).isPresent()) {
            String errMsg = String.format("The user with the username: %s already exist!", email);
            log.error(errMsg);
            throw new UsernameAlreadyExistException(errMsg);
        }
        AppUser user = AppUser.builder()
                .firstname(userRegistrationDto.firstname())
                .lastname(userRegistrationDto.lastname())
                .email(email)
                .password(passwordEncoder.encode(userRegistrationDto.password()))
                .role(Role.ROLE_USER)
                .enabled(false)
                .locked(false)
                .build();
        userRepository.save(user);
        log.info("The user with the username: {} has been successfully saved", email);
    }

    public AccessAndRefreshTokensDto authenticateUser(UserAuthDto userAuthDto) {
        String username = userAuthDto.email();
        Optional<AppUser> userOptional = userRepository.findByEmail(username);
        if (userOptional.isEmpty() || !passwordEncoder.matches(userAuthDto.password(), userOptional.get().getPassword())) {
            throw new UserNotFoundException("Bad user credentials!");
        }
        AppUser user = userOptional.get();
        if (!user.isEnabled()) {
            log.error("The account with the username: {} is not activated!", username);
            throw new AccountActivationException("The account is not activated!");
        }
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, userAuthDto.password()));
        jwtTokenService.revokeAllUserAuthTokens(user);
        return jwtTokenService.generateAuthTokens(user);
    }

    public void changeUserPassword(AppUser user, String newPassword) {
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            log.error("The new password matches the old password for user with username: {}", user.getUsername());
            throw new InvalidPasswordException("The new password matches old password!");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
