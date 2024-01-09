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
            log.error("User with username: {} already exist!", email);
            throw new UsernameAlreadyExistException(String.format("User with username: %s already exist!", email));
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

    public AccessAndRefreshTokensDto authenticateUser(UserAuthDto userAuthDto) throws UserNotFoundException, AccountActivationException {
        String username = userAuthDto.email();
        Optional<AppUser> user = userRepository.findByEmail(username);
        if (user.isEmpty() || !passwordEncoder.matches(user.get().getPassword(), userAuthDto.password())) {
            log.error("Bad user credentials!");
            throw new UserNotFoundException("Bad user credentials!");
        }
        if (!user.get().isEnabled()) {
            log.error("Account with username: {} is not activated!", username);
            throw new AccountActivationException("Account is not activated!");
        }
        String email = userAuthDto.email();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, userAuthDto.password()));
        jwtTokenService.revokeAllUserAuthTokens(user.get());
        return jwtTokenService.generateAuthTokens(user.get());
    }

    @Override
    public void activateUserAccount(AppUser user) {
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    public void changeUserPassword(AppUser user, String newPassword) throws InvalidPasswordException {
        if(passwordEncoder.matches(newPassword, user.getPassword())){
            throw new InvalidPasswordException("New password matches old password!");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
