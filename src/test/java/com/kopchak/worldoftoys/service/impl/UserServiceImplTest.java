package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.user.UserRegistrationDto;
import com.kopchak.worldoftoys.exception.InvalidConfirmationTokenException;
import com.kopchak.worldoftoys.exception.UserNotFoundException;
import com.kopchak.worldoftoys.model.token.ConfirmationToken;
import com.kopchak.worldoftoys.model.user.AppUser;
import com.kopchak.worldoftoys.repository.token.ConfirmTokenRepository;
import com.kopchak.worldoftoys.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ConfirmTokenRepository confirmationTokenRepository;
    @InjectMocks
    private UserServiceImpl userService;
    private String userEmail;
    private AppUser user;
    private ConfirmationToken confirmationToken;
    private String confirmToken;
    private String userPassword;

    @BeforeEach
    void setUp() {
        userEmail = "test@gmail.com";
        user = AppUser
                .builder()
                .enabled(true)
                .password("password")
                .build();
        confirmationToken = ConfirmationToken.builder().user(user).build();
        confirmToken = "confirm-token";
        userPassword = "new-password";
    }

    @Test
    public void registerUser_UserRegistrationDto() {
        UserRegistrationDto userRegistrationDto = UserRegistrationDto
                .builder()
                .firstname("Firstname")
                .lastname("Lastname")
                .email(userEmail)
                .password("password")
                .build();

        userService.registerUser(userRegistrationDto);

        verify(userRepository).save(any());
    }

    @Test
    public void isUserRegistered_ExistingUserEmail_ReturnsTrue() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        boolean isUserRegistered = userService.isUserRegistered(userEmail);

        assertTrue(isUserRegistered);
    }

    @Test
    public void isUserActivated_ActivatedUser_ReturnsTrue() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        boolean isUserActivated = userService.isUserActivated(userEmail);

        assertTrue(isUserActivated);
    }

    @Test
    public void isUserActivated_NonExistingUser_ThrowsUserNotFoundException() {
        ResponseStatusException exception = assertThrows(UserNotFoundException.class, () ->
                userService.isUserActivated(userEmail));

        String expectedMessage = "User with this username does not exist!";
        String actualMessage = exception.getReason();
        int expectedStatusCode = HttpStatus.NOT_FOUND.value();
        int actualStatusCode = exception.getStatusCode().value();

        assertEquals(expectedMessage, actualMessage);
        assertEquals(expectedStatusCode, actualStatusCode);
    }

    @Test
    public void isNewPasswordMatchOldPassword_ExistingConfirmTokenAndMatchingPasswords_ReturnsTrue() {
        when(confirmationTokenRepository.findByToken(confirmToken)).thenReturn(Optional.of(confirmationToken));
        when(passwordEncoder.matches(userPassword, user.getPassword())).thenReturn(true);

        boolean isNewPasswordMatchOldPassword = userService.isNewPasswordMatchOldPassword(confirmToken, userPassword);

        assertTrue(isNewPasswordMatchOldPassword);
    }

    @Test
    public void isNewPasswordMatchOldPassword_NonExistingConfirmToken_ThrowsInvalidConfirmationTokenException() {
        ResponseStatusException exception = assertThrows(InvalidConfirmationTokenException.class, () ->
                userService.isNewPasswordMatchOldPassword(confirmToken, userPassword));

        String expectedMessage = "This confirmation token is invalid!";
        String actualMessage = exception.getReason();
        int expectedStatusCode = HttpStatus.BAD_REQUEST.value();
        int actualStatusCode = exception.getStatusCode().value();

        assertEquals(expectedMessage, actualMessage);
        assertEquals(expectedStatusCode, actualStatusCode);
    }

    @Test
    public void isPasswordsMatch_MatchingPasswords_ReturnsTrue() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(userPassword, user.getPassword())).thenReturn(true);

        boolean isPasswordsMatch = userService.isPasswordsMatch(userEmail, userPassword);

        assertTrue(isPasswordsMatch);
    }

    @Test
    public void isPasswordsMatch_NonExistingUser_ThrowsUserNotFoundException() {
        ResponseStatusException exception = assertThrows(UserNotFoundException.class, () ->
                userService.isPasswordsMatch(userEmail, userPassword));

        String expectedMessage = "User with this username does not exist!";
        String actualMessage = exception.getReason();
        int expectedStatusCode = HttpStatus.NOT_FOUND.value();
        int actualStatusCode = exception.getStatusCode().value();

        assertEquals(expectedMessage, actualMessage);
        assertEquals(expectedStatusCode, actualStatusCode);
    }

    @Test
    public void activateUserAccount_UnactivatedAppUser() {
        user.setEnabled(false);

        userService.activateUserAccount(user);

        verify(userRepository).save(user);
        assertTrue(user.getEnabled());
    }

    @Test
    public void changeUserPassword_AppUserAndNewPassword() {
        when(passwordEncoder.encode(userPassword)).thenReturn(userPassword);

        userService.changeUserPassword(user, userPassword);

        verify(userRepository).save(user);
        assertEquals(user.getPassword(), userPassword);
    }
}