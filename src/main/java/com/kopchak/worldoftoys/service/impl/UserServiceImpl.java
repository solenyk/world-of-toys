package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.token.AccessAndRefreshTokensDto;
import com.kopchak.worldoftoys.dto.user.UserAuthDto;
import com.kopchak.worldoftoys.dto.user.UserRegistrationDto;
import com.kopchak.worldoftoys.exception.exception.AccountActivationException;
import com.kopchak.worldoftoys.exception.exception.InvalidPasswordException;
import com.kopchak.worldoftoys.exception.exception.UserNotFoundException;
import com.kopchak.worldoftoys.exception.exception.UsernameAlreadyExistException;
import com.kopchak.worldoftoys.model.user.AppUser;
import com.kopchak.worldoftoys.model.user.Role;
import com.kopchak.worldoftoys.repository.user.UserRepository;
import com.kopchak.worldoftoys.service.JwtTokenService;
import com.kopchak.worldoftoys.service.UserService;
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
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final AuthenticationManager authenticationManager;

    @Override
    public void registerUser(UserRegistrationDto userRegistrationDto) throws UsernameAlreadyExistException {
        String email = userRegistrationDto.email();
        if (userRepository.findByEmail(email).isPresent()) {
            String errMsg = String.format("User with username: %s already exist!", email);
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
        log.info("User: {} has been successfully saved", email);
    }

    @Override
    public AccessAndRefreshTokensDto authenticateUser(UserAuthDto userAuthDto) throws UserNotFoundException, AccountActivationException {
        String username = userAuthDto.email();
        Optional<AppUser> userOptional = userRepository.findByEmail(username);
        if (userOptional.isEmpty() || !passwordEncoder.matches(userAuthDto.password(), userOptional.get().getPassword())) {
            log.error("Authentication failed: bad user credentials");
            throw new UserNotFoundException("Bad user credentials!");
        }
        AppUser user = userOptional.get();
        if (!user.isEnabled()) {
            log.error("Account with username: {} is not activated!", username);
            throw new AccountActivationException("Account is not activated!");
        }
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, userAuthDto.password()));
        jwtTokenService.revokeAllUserAuthTokens(user);
        return jwtTokenService.generateAuthTokens(user);
    }

    @Override
    public void changeUserPassword(AppUser user, String newPassword) throws InvalidPasswordException {
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            log.error("New password matches old password for user with username: {}", user.getUsername());
            throw new InvalidPasswordException("New password matches old password!");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
