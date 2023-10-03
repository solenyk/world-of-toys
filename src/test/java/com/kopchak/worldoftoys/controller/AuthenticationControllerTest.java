package com.kopchak.worldoftoys.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kopchak.worldoftoys.dto.error.ErrorResponseDto;
import com.kopchak.worldoftoys.dto.token.ConfirmTokenDto;
import com.kopchak.worldoftoys.dto.user.UserRegistrationDto;
import com.kopchak.worldoftoys.dto.user.UsernameDto;
import com.kopchak.worldoftoys.exception.AccountIsAlreadyActivatedException;
import com.kopchak.worldoftoys.exception.InvalidConfirmationTokenException;
import com.kopchak.worldoftoys.exception.UserNotFoundException;
import com.kopchak.worldoftoys.exception.UsernameAlreadyExistException;
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
    private ResponseStatusException usernameAlreadyExistException;
    private ResponseStatusException invalidConfirmationTokenException;
    private ResponseStatusException userNotFoundException;
    private ResponseStatusException accountIsAlreadyActivatedException;
    private UsernameDto usernameDto;

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
        usernameDto = UsernameDto.builder().email(username).build();
        usernameAlreadyExistException = new UsernameAlreadyExistException(HttpStatus.BAD_REQUEST, "This username already exist!");
        invalidConfirmationTokenException = new InvalidConfirmationTokenException(HttpStatus.BAD_REQUEST, "This confirmation token is invalid!");
        userNotFoundException = new UserNotFoundException(HttpStatus.NOT_FOUND, "User with this username does not exist!");
        accountIsAlreadyActivatedException = new AccountIsAlreadyActivatedException(HttpStatus.CONFLICT, "Account is already activated!");
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
    public void registerUser_UserRegistrationDtoWithRegisteredUserEmail_ReturnsBadRequestStatus() throws Exception {
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
    public void activateAccount_InvalidConfirmToken_ReturnsBadRequestStatus() throws Exception {
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
        when(confirmationTokenService.isNoActiveConfirmationToken(username,activationTokenType)).thenReturn(true);
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
    public void resendVerificationEmail_NotRegisteredUser_ReturnsNotFoundStatus() throws Exception {
        when(userService.isUserRegistered(username)).thenReturn(false);

        ResultActions response = mockMvc.perform(post("/api/v1/auth/resend-verification-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usernameDto)));

        verify(confirmationTokenService, never()).createConfirmationToken(any(), any());
        verify(emailSenderService, never()).sendEmail(any(), any(), any());

        ErrorResponseDto errorResponseDto = responseStatusExceptionToErrorResponseDto(userNotFoundException);

        response.andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().json(objectMapper.writeValueAsString(errorResponseDto)));
    }
    @Test
    public void resendVerificationEmail_RegisteredAndActivatedUser_ReturnsConflictStatus() throws Exception {
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
        when(confirmationTokenService.isNoActiveConfirmationToken(username,resetPasswordTokenType)).thenReturn(true);
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
    public void sendResetPasswordEmail_NotRegisteredUser_ReturnsNoContentStatus() throws Exception {
        when(userService.isUserRegistered(username)).thenReturn(false);

        ResultActions response = mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usernameDto)));

        verify(confirmationTokenService, never()).createConfirmationToken(any(), any());
        verify(emailSenderService, never()).sendEmail(any(), any(), any());

        ErrorResponseDto errorResponseDto = responseStatusExceptionToErrorResponseDto(userNotFoundException);

        response.andExpect(MockMvcResultMatchers.status().isNotFound())
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