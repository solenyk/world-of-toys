package com.kopchak.worldoftoys.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kopchak.worldoftoys.dto.error.ErrorResponseDto;
import com.kopchak.worldoftoys.dto.token.AccessAndRefreshTokensDto;
import com.kopchak.worldoftoys.dto.token.ConfirmTokenDto;
import com.kopchak.worldoftoys.dto.user.ResetPasswordDto;
import com.kopchak.worldoftoys.dto.user.UserAuthDto;
import com.kopchak.worldoftoys.dto.user.UserRegistrationDto;
import com.kopchak.worldoftoys.dto.user.UsernameDto;
import com.kopchak.worldoftoys.exception.*;
import com.kopchak.worldoftoys.model.token.ConfirmationTokenType;
import com.kopchak.worldoftoys.service.ConfirmationTokenService;
import com.kopchak.worldoftoys.service.EmailSenderService;
import com.kopchak.worldoftoys.service.JwtTokenService;
import com.kopchak.worldoftoys.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(controllers = AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private ConfirmationTokenService confirmationTokenService;
    @MockBean
    private EmailSenderService emailSenderService;
    @MockBean
    private JwtTokenService jwtTokenService;
    @MockBean
    private AuthenticationManager authenticationManager;

    @Autowired
    private ObjectMapper objectMapper;

    private UserRegistrationDto userRegistrationDto;
    private ConfirmationTokenType activationTokenType;
    private ConfirmationTokenType resetPasswordTokenType;
    private String username;
    private String confirmToken;
    private ConfirmTokenDto confirmTokenDto;
    private UserAuthDto userAuthDto;
    private ResponseStatusException usernameAlreadyExistException;
    private ResponseStatusException invalidConfirmationTokenException;
    private ResponseStatusException userNotFoundExceptionUserIsNotExist;
    private ResponseStatusException userNotFoundExceptionBadCredentials;
    private ResponseStatusException userNotFoundExceptionAccountIsNotActivated;
    private ResponseStatusException accountIsAlreadyActivatedException;
    private ResponseStatusException invalidPasswordException;
    private UsernameDto usernameDto;
    private ResetPasswordDto resetPasswordDto;
    private AccessAndRefreshTokensDto accessAndRefreshTokensDto;

    @BeforeEach
    public void setUp() {
        username = "test@gmail.com";
        userRegistrationDto = UserRegistrationDto
                .builder()
                .firstname("Firstname")
                .lastname("Lastname")
                .email(username)
                .password("password")
                .build();
        activationTokenType = ConfirmationTokenType.ACTIVATION;
        resetPasswordTokenType = ConfirmationTokenType.RESET_PASSWORD;
        confirmToken = "confirm-token";
        confirmTokenDto = ConfirmTokenDto
                .builder()
                .token(confirmToken)
                .build();
        userAuthDto = UserAuthDto
                .builder()
                .email(username)
                .password("password")
                .build();
        usernameDto = UsernameDto.builder().email(username).build();
        resetPasswordDto = ResetPasswordDto.builder().password("new-password").build();
        accessAndRefreshTokensDto = AccessAndRefreshTokensDto
                .builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();
        usernameAlreadyExistException = new UsernameAlreadyExistException(HttpStatus.BAD_REQUEST, "This username already exist!");
        invalidConfirmationTokenException = new InvalidConfirmationTokenException(HttpStatus.BAD_REQUEST, "This confirmation token is invalid!");
        userNotFoundExceptionUserIsNotExist = new UserNotFoundException(HttpStatus.NOT_FOUND, "User with this username does not exist!");
        userNotFoundExceptionBadCredentials = new UserNotFoundException(HttpStatus.UNAUTHORIZED, "Bad user credentials!");
        userNotFoundExceptionAccountIsNotActivated = new UserNotFoundException(HttpStatus.FORBIDDEN, "Account is not activated!");
        accountIsAlreadyActivatedException = new AccountIsAlreadyActivatedException(HttpStatus.CONFLICT, "Account is already activated!");
        invalidPasswordException = new InvalidPasswordException(HttpStatus.BAD_REQUEST, "New password matches old password!");
    }

    @Test
    public void registerUser_UserRegistrationDtoWithUnregisteredUserEmail_ReturnsCreatedStatus() throws Exception {
        when(userService.isUserRegistered(username)).thenReturn(false);
        doNothing().when(userService).registerUser(userRegistrationDto);
        when(confirmationTokenService.createConfirmationToken(username, activationTokenType))
                .thenReturn(confirmTokenDto);
        doNothing().when(emailSenderService).sendEmail(username, confirmTokenDto.getToken(), activationTokenType);

        ResultActions response = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRegistrationDto)));

        verify(userService).registerUser(any());
        verify(confirmationTokenService).createConfirmationToken(userRegistrationDto.getEmail(), activationTokenType);
        verify(emailSenderService).sendEmail(userRegistrationDto.getEmail(), confirmTokenDto.getToken(),
                activationTokenType);

        response.andExpect(MockMvcResultMatchers.status().isCreated())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void registerUser_UserRegistrationDtoWithRegisteredUserEmail_ReturnsBadRequestStatusAndErrorResponseDto() throws Exception {
        when(userService.isUserRegistered(userRegistrationDto.getEmail())).thenReturn(true);

        ResultActions response = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRegistrationDto)));

        verify(userService, never()).registerUser(any());
        verify(confirmationTokenService, never()).createConfirmationToken(any(), any());
        verify(emailSenderService, never()).sendEmail(any(), any(), any());

        ErrorResponseDto errorResponseDto = responseStatusExceptionToErrorResponseDto(usernameAlreadyExistException);

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(errorResponseDto)));
    }

    @Test
    public void activateAccount_ValidConfirmToken_ReturnsNoContentStatus() throws Exception {
        when(confirmationTokenService.isConfirmationTokenInvalid(confirmToken, activationTokenType)).thenReturn(false);
        doNothing().when(confirmationTokenService).activateAccountUsingActivationToken(confirmToken);

        ResultActions response = mockMvc.perform(get("/api/v1/auth/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .param("token", confirmToken));

        verify(confirmationTokenService).activateAccountUsingActivationToken(confirmToken);

        response.andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void activateAccount_InvalidConfirmToken_ReturnsBadRequestStatusAndErrorResponseDto() throws Exception {
        when(confirmationTokenService.isConfirmationTokenInvalid(confirmToken, activationTokenType)).thenReturn(true);

        ResultActions response = mockMvc.perform(get("/api/v1/auth/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .param("token", confirmToken));

        verify(confirmationTokenService, never()).activateAccountUsingActivationToken(any());

        ErrorResponseDto errorResponseDto = responseStatusExceptionToErrorResponseDto(invalidConfirmationTokenException);

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(errorResponseDto)));
    }

    @Test
    public void resendVerificationEmail_RegisteredAndNotActivatedUser_ReturnsNoContentStatus() throws Exception {
        when(userService.isUserRegistered(username)).thenReturn(true);
        when(userService.isUserActivated(username)).thenReturn(false);
        when(confirmationTokenService.isNoActiveConfirmationToken(username, activationTokenType)).thenReturn(true);
        when(confirmationTokenService.createConfirmationToken(username, activationTokenType)).thenReturn(confirmTokenDto);
        doNothing().when(emailSenderService).sendEmail(username, confirmToken, activationTokenType);

        ResultActions response = mockMvc.perform(post("/api/v1/auth/resend-verification-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usernameDto)));

        verify(confirmationTokenService).createConfirmationToken(username, activationTokenType);
        verify(emailSenderService).sendEmail(username, confirmToken, activationTokenType);

        response.andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void resendVerificationEmail_NotRegisteredUser_ReturnsNotFoundStatusAndErrorResponseDto() throws Exception {
        when(userService.isUserRegistered(username)).thenReturn(false);

        ResultActions response = mockMvc.perform(post("/api/v1/auth/resend-verification-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usernameDto)));

        verify(confirmationTokenService, never()).createConfirmationToken(any(), any());
        verify(emailSenderService, never()).sendEmail(any(), any(), any());

        ErrorResponseDto errorResponseDto = responseStatusExceptionToErrorResponseDto(userNotFoundExceptionUserIsNotExist);

        response.andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().json(objectMapper.writeValueAsString(errorResponseDto)));
    }

    @Test
    public void resendVerificationEmail_RegisteredAndActivatedUser_ReturnsConflictStatusAndErrorResponseDto() throws Exception {
        when(userService.isUserRegistered(username)).thenReturn(true);
        when(userService.isUserActivated(username)).thenReturn(true);

        ResultActions response = mockMvc.perform(post("/api/v1/auth/resend-verification-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usernameDto)));

        verify(confirmationTokenService, never()).createConfirmationToken(any(), any());
        verify(emailSenderService, never()).sendEmail(any(), any(), any());

        ErrorResponseDto errorResponseDto = responseStatusExceptionToErrorResponseDto(accountIsAlreadyActivatedException);

        response.andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(content().json(objectMapper.writeValueAsString(errorResponseDto)));
    }

    @Test
    public void sendResetPasswordEmail_RegisteredUser_ReturnsNoContentStatus() throws Exception {
        when(userService.isUserRegistered(username)).thenReturn(true);
        when(confirmationTokenService.isNoActiveConfirmationToken(username, resetPasswordTokenType)).thenReturn(true);
        when(confirmationTokenService.createConfirmationToken(username, resetPasswordTokenType)).thenReturn(confirmTokenDto);
        doNothing().when(emailSenderService).sendEmail(username, confirmToken, resetPasswordTokenType);

        ResultActions response = mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usernameDto)));

        verify(confirmationTokenService).createConfirmationToken(username, resetPasswordTokenType);
        verify(emailSenderService).sendEmail(username, confirmToken, resetPasswordTokenType);

        response.andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void sendResetPasswordEmail_NotRegisteredUser_ReturnsNotFoundStatusAndErrorResponseDto() throws Exception {
        when(userService.isUserRegistered(username)).thenReturn(false);

        ResultActions response = mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usernameDto)));

        verify(confirmationTokenService, never()).createConfirmationToken(any(), any());
        verify(emailSenderService, never()).sendEmail(any(), any(), any());

        ErrorResponseDto errorResponseDto = responseStatusExceptionToErrorResponseDto(userNotFoundExceptionUserIsNotExist);

        response.andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().json(objectMapper.writeValueAsString(errorResponseDto)));
    }

    @Test
    public void changePassword_ValidConfirmTokenAndNewPasswordNotMatchOldPassword_ReturnsNoContentStatus() throws Exception {
        when(confirmationTokenService.isConfirmationTokenInvalid(confirmToken, resetPasswordTokenType)).thenReturn(false);
        when(userService.isNewPasswordMatchOldPassword(confirmToken, resetPasswordDto.getPassword())).thenReturn(false);
        doNothing().when(confirmationTokenService).changePasswordUsingResetToken(confirmToken, resetPasswordDto);

        ResultActions response = mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .param("token", confirmToken)
                .content(objectMapper.writeValueAsString(resetPasswordDto)));

        verify(confirmationTokenService).changePasswordUsingResetToken(eq(confirmToken), any());

        response.andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void changePassword_ValidConfirmTokenAndNewPasswordMatchOldPassword_ReturnsBadRequestStatusAndErrorResponseDto() throws Exception {
        when(confirmationTokenService.isConfirmationTokenInvalid(confirmToken, resetPasswordTokenType)).thenReturn(false);
        when(userService.isNewPasswordMatchOldPassword(confirmToken, resetPasswordDto.getPassword())).thenReturn(true);

        ResultActions response = mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .param("token", confirmToken)
                .content(objectMapper.writeValueAsString(resetPasswordDto)));

        verify(confirmationTokenService, never()).changePasswordUsingResetToken(any(), any());

        ErrorResponseDto errorResponseDto = responseStatusExceptionToErrorResponseDto(invalidPasswordException);

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(errorResponseDto)));
    }

    @Test
    public void changePassword_InvalidConfirmToken_ReturnsBadRequestStatusAndErrorResponseDto() throws Exception {
        when(confirmationTokenService.isConfirmationTokenInvalid(confirmToken, resetPasswordTokenType)).thenReturn(true);

        ResultActions response = mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .param("token", confirmToken)
                .content(objectMapper.writeValueAsString(resetPasswordDto)));

        verify(confirmationTokenService, never()).changePasswordUsingResetToken(any(), any());

        ErrorResponseDto errorResponseDto = responseStatusExceptionToErrorResponseDto(invalidConfirmationTokenException);

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(errorResponseDto)));
    }

    @Test
    public void authenticate_RegisteredAndActivatedUser_ReturnsOkStatusAndAccessAndRefreshTokensDto() throws Exception {
        when(userService.isUserRegistered(username)).thenReturn(true);
        when(userService.isPasswordsMatch(username, userAuthDto.getPassword())).thenReturn(true);
        when(userService.isUserActivated(username)).thenReturn(true);

        doNothing().when(jwtTokenService).revokeAllUserAuthTokens(username);
        when(jwtTokenService.generateAuthTokens(username)).thenReturn(accessAndRefreshTokensDto);

        ResultActions response = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userAuthDto)));

        verify(authenticationManager).authenticate(any());
        verify(jwtTokenService).revokeAllUserAuthTokens(username);
        verify(jwtTokenService).generateAuthTokens(username);

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(accessAndRefreshTokensDto)));
    }

    @Test
    public void authenticate_RegisteredAndNotActivatedUser_ReturnsForbiddenStatusAndErrorResponseDto() throws Exception {
        when(userService.isUserRegistered(username)).thenReturn(true);
        when(userService.isPasswordsMatch(username, userAuthDto.getPassword())).thenReturn(true);
        when(userService.isUserActivated(username)).thenReturn(false);

        ResultActions response = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userAuthDto)));

        verify(authenticationManager, never()).authenticate(any());
        verify(jwtTokenService, never()).revokeAllUserAuthTokens(any());
        verify(jwtTokenService, never()).generateAuthTokens(any());

        ErrorResponseDto errorResponseDto = responseStatusExceptionToErrorResponseDto(userNotFoundExceptionAccountIsNotActivated);

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().json(objectMapper.writeValueAsString(errorResponseDto)));
    }

    @Test
    public void authenticate_NotRegisteredUser_ReturnsUnauthorizedStatusAndErrorResponseDto() throws Exception {
        when(userService.isUserRegistered(username)).thenReturn(false);

        ResultActions response = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userAuthDto)));

        verify(authenticationManager, never()).authenticate(any());
        verify(jwtTokenService, never()).revokeAllUserAuthTokens(any());
        verify(jwtTokenService, never()).generateAuthTokens(any());

        ErrorResponseDto errorResponseDto = responseStatusExceptionToErrorResponseDto(userNotFoundExceptionBadCredentials);

        response.andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().json(objectMapper.writeValueAsString(errorResponseDto)));
    }


    private ErrorResponseDto responseStatusExceptionToErrorResponseDto(ResponseStatusException ex) {
        int statusCode = ex.getStatusCode().value();
        return ErrorResponseDto
                .builder()
                .error(HttpStatus.valueOf(statusCode).name())
                .status(statusCode)
                .message(ex.getReason())
                .build();
    }
}