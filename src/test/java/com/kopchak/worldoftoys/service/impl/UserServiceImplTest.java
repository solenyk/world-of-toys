package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.domain.user.AppUser;
import com.kopchak.worldoftoys.dto.token.AccessAndRefreshTokensDto;
import com.kopchak.worldoftoys.dto.user.UserAuthDto;
import com.kopchak.worldoftoys.dto.user.UserRegistrationDto;
import com.kopchak.worldoftoys.exception.exception.user.AccountActivationException;
import com.kopchak.worldoftoys.exception.exception.user.InvalidPasswordException;
import com.kopchak.worldoftoys.exception.exception.user.UserNotFoundException;
import com.kopchak.worldoftoys.exception.exception.user.UsernameAlreadyExistException;
import com.kopchak.worldoftoys.repository.user.UserRepository;
import com.kopchak.worldoftoys.service.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private AuthenticationManager authenticationManager;
    @InjectMocks
    private UserServiceImpl userService;
    private final static String USER_EMAIL = "test@gmail.com";
    private final static String OLD_USER_PASSWORD = "password";
    private final static String NEW_USER_PASSWORD = "new-password";
    private UserRegistrationDto userRegistrationDto;
    private AppUser user;
    private UserAuthDto userAuthDto;

    @BeforeEach
    void setUp() {
        userRegistrationDto = UserRegistrationDto
                .builder()
                .firstname("Firstname")
                .lastname("Lastname")
                .email(USER_EMAIL)
                .password(OLD_USER_PASSWORD)
                .build();
        user = AppUser
                .builder()
                .enabled(true)
                .password(OLD_USER_PASSWORD)
                .build();
        userAuthDto = new UserAuthDto(USER_EMAIL, OLD_USER_PASSWORD);
    }

    @Test
    public void registerUser_UserIsNotPresent() throws UsernameAlreadyExistException {
        userService.registerUser(userRegistrationDto);

        verify(userRepository).save(any());
    }

    @Test
    public void registerUser_UserIsPresent_ThrowsUsernameAlreadyExistException() {
        String usernameAlreadyExistExceptionMsg = String.format("The user with the username: %s already exist!", USER_EMAIL);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

        assertException(UsernameAlreadyExistException.class, usernameAlreadyExistExceptionMsg,
                () -> userService.registerUser(userRegistrationDto));
    }

    @Test
    public void authenticateUser_UserIsPresentAndEnabledPasswordMatches() throws UserNotFoundException, AccountActivationException {
        var expectedAccessAndRefreshTokensDto = AccessAndRefreshTokensDto.builder().build();

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq(OLD_USER_PASSWORD), eq(OLD_USER_PASSWORD))).thenReturn(true);
        doNothing().when(jwtTokenService).revokeAllUserAuthTokens(eq(user));
        when(jwtTokenService.generateAuthTokens(user)).thenReturn(expectedAccessAndRefreshTokensDto);

        var actualAccessAndRefreshTokensDto = userService.authenticateUser(userAuthDto);

        verify(authenticationManager).authenticate(any());
        verify(jwtTokenService).revokeAllUserAuthTokens(eq(user));

        assertThat(actualAccessAndRefreshTokensDto).isNotNull();
        assertThat(actualAccessAndRefreshTokensDto).isEqualTo(expectedAccessAndRefreshTokensDto);
    }

    @Test
    public void authenticateUser_UserIsPresentAndIsNotEnabledPasswordMatches_ThrowsAccountActivationException() {
        user.setEnabled(false);
        String accountActivationExceptionMsg = "The account is not activated!";

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq(OLD_USER_PASSWORD), eq(OLD_USER_PASSWORD))).thenReturn(true);

        assertException(AccountActivationException.class, accountActivationExceptionMsg,
                () -> userService.authenticateUser(userAuthDto));

        verify(authenticationManager, never()).authenticate(any());
        verify(jwtTokenService, never()).revokeAllUserAuthTokens(eq(user));
        verify(jwtTokenService, never()).generateAuthTokens(eq(user));
    }

    @Test
    public void authenticateUser_UserNotIsPresent_ThrowsUserNotFoundException() {
        String userNotFoundExceptionMsg = "Bad user credentials!";

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

        assertException(UserNotFoundException.class, userNotFoundExceptionMsg,
                () -> userService.authenticateUser(userAuthDto));

        verify(authenticationManager, never()).authenticate(any());
        verify(jwtTokenService, never()).revokeAllUserAuthTokens(eq(user));
        verify(jwtTokenService, never()).generateAuthTokens(eq(user));
    }

    @Test
    public void changeUserPassword_NewPassword() throws InvalidPasswordException {
        when(passwordEncoder.encode(NEW_USER_PASSWORD)).thenReturn(NEW_USER_PASSWORD);

        userService.changeUserPassword(user, NEW_USER_PASSWORD);

        verify(userRepository).save(user);
        assertEquals(user.getPassword(), NEW_USER_PASSWORD);
    }

    @Test
    public void changeUserPassword_OldPassword_ThrowsInvalidPasswordException() {
        String invalidPasswordExceptionMsg = "The new password matches old password!";
        when(passwordEncoder.matches(eq(OLD_USER_PASSWORD), eq(OLD_USER_PASSWORD))).thenReturn(true);

        assertException(InvalidPasswordException.class, invalidPasswordExceptionMsg,
                () -> userService.changeUserPassword(user, OLD_USER_PASSWORD));

        verify(userRepository, never()).save(user);
    }

    private void assertException(Class<? extends Exception> expectedExceptionType,
                                 String expectedMessage, Executable executable) {
        Exception exception = assertThrows(expectedExceptionType, executable);
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }
}