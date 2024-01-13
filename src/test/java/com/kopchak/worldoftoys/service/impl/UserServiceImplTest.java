package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.user.UserRegistrationDto;
import com.kopchak.worldoftoys.exception.InvalidConfirmationTokenException1;
import com.kopchak.worldoftoys.exception.UserNotFoundException1;
import com.kopchak.worldoftoys.domain.token.ConfirmationToken;
import com.kopchak.worldoftoys.domain.user.AppUser;
import com.kopchak.worldoftoys.repository.token.ConfirmTokenRepository;
import com.kopchak.worldoftoys.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
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
    private String confirmToken;
    private String userPassword;
    private String userNotFoundExceptionMsg;

    @BeforeEach
    void setUp() {
        userEmail = "test@gmail.com";
        user = AppUser
                .builder()
                .enabled(true)
                .password("password")
                .build();
        confirmToken = "confirm-token";
        userPassword = "new-password";
        userNotFoundExceptionMsg = "User with this username does not exist!";
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
        assertResponseStatusException(UserNotFoundException1.class, userNotFoundExceptionMsg, HttpStatus.NOT_FOUND, () ->
                userService.isUserActivated(userEmail));
    }

    @Test
    public void isNewPasswordMatchOldPassword_ExistingConfirmTokenAndMatchingPasswords_ReturnsTrue() {
        ConfirmationToken confirmationToken = ConfirmationToken.builder().user(user).build();

        when(confirmationTokenRepository.findByToken(confirmToken)).thenReturn(Optional.of(confirmationToken));
        when(passwordEncoder.matches(userPassword, user.getPassword())).thenReturn(true);

        boolean isNewPasswordMatchOldPassword = userService.isNewPasswordMatchOldPassword(confirmToken, userPassword);

        assertTrue(isNewPasswordMatchOldPassword);
    }

    @Test
    public void isNewPasswordMatchOldPassword_NonExistingConfirmToken_ThrowsInvalidConfirmationTokenException() {
        String  invalidConfirmationTokenExceptionMsg = "This confirmation token is invalid!";

        assertResponseStatusException(InvalidConfirmationTokenException1.class, invalidConfirmationTokenExceptionMsg,
                HttpStatus.BAD_REQUEST, () -> userService.isNewPasswordMatchOldPassword(confirmToken, userPassword));
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
        assertResponseStatusException(UserNotFoundException1.class, userNotFoundExceptionMsg, HttpStatus.NOT_FOUND, () ->
                userService.isPasswordsMatch(userEmail, userPassword));
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

    private void assertResponseStatusException(Class<? extends ResponseStatusException> expectedExceptionType,
                                               String expectedMessage, HttpStatus expectedHttpStatus,
                                               Executable executable) {
        ResponseStatusException exception = assertThrows(expectedExceptionType, executable);

        String actualMessage = exception.getReason();
        int expectedStatusCode = expectedHttpStatus.value();
        int actualStatusCode = exception.getStatusCode().value();

        assertEquals(expectedMessage, actualMessage);
        assertEquals(expectedStatusCode, actualStatusCode);
    }
}