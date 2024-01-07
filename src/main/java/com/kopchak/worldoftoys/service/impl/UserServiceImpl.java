package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.user.UserRegistrationDto;
import com.kopchak.worldoftoys.exception.InvalidConfirmationTokenException1;
import com.kopchak.worldoftoys.exception.UserNotFoundException1;
import com.kopchak.worldoftoys.exception.exception.UsernameAlreadyExistException;
import com.kopchak.worldoftoys.model.token.ConfirmationToken;
import com.kopchak.worldoftoys.model.user.AppUser;
import com.kopchak.worldoftoys.model.user.Role;
import com.kopchak.worldoftoys.repository.token.ConfirmTokenRepository;
import com.kopchak.worldoftoys.repository.user.UserRepository;
import com.kopchak.worldoftoys.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConfirmTokenRepository confirmationTokenRepository;

    @Override
    public void registerUser(UserRegistrationDto userRegistrationDto) throws UsernameAlreadyExistException {
        String email = userRegistrationDto.email();
        if (isUserRegistered(email)) {
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

    @Override
    public boolean isUserRegistered(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public boolean isUserActivated(String email) {
        AppUser user = userRepository.findByEmail(email).orElseThrow(() ->
                new UserNotFoundException1(HttpStatus.NOT_FOUND, "User with this username does not exist!"));
        return user.getEnabled();
    }

    @Override
    public boolean isNewPasswordMatchOldPassword(String resetPasswordToken, String newPassword) {
        ConfirmationToken confirmationToken = confirmationTokenRepository.findByToken(resetPasswordToken).orElseThrow(
                () -> new InvalidConfirmationTokenException1(HttpStatus.BAD_REQUEST, "This confirmation token is invalid!"));
        AppUser user = confirmationToken.getUser();
        return passwordEncoder.matches(newPassword, user.getPassword());
    }

    @Override
    public boolean isPasswordsMatch(String email, String password) {
        AppUser user = userRepository.findByEmail(email).orElseThrow(() ->
                new UserNotFoundException1(HttpStatus.NOT_FOUND, "User with this username does not exist!"));
        return passwordEncoder.matches(password, user.getPassword());
    }

    @Override
    public void activateUserAccount(AppUser user) {
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    public void changeUserPassword(AppUser user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
