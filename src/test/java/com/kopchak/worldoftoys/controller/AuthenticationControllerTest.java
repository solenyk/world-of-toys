package com.kopchak.worldoftoys.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kopchak.worldoftoys.domain.token.confirm.ConfirmationTokenType;
import com.kopchak.worldoftoys.dto.error.ExceptionDto;
import com.kopchak.worldoftoys.dto.token.AccessAndRefreshTokensDto;
import com.kopchak.worldoftoys.dto.token.AuthTokenDto;
import com.kopchak.worldoftoys.dto.token.ConfirmTokenDto;
import com.kopchak.worldoftoys.dto.user.ResetPasswordDto;
import com.kopchak.worldoftoys.dto.user.UserAuthDto;
import com.kopchak.worldoftoys.dto.user.UserRegistrationDto;
import com.kopchak.worldoftoys.dto.user.UsernameDto;
import com.kopchak.worldoftoys.exception.exception.email.MessageSendingException;
import com.kopchak.worldoftoys.exception.exception.token.InvalidConfirmationTokenException;
import com.kopchak.worldoftoys.exception.exception.token.TokenAlreadyExistException;
import com.kopchak.worldoftoys.exception.exception.user.UserNotFoundException;
import com.kopchak.worldoftoys.exception.exception.user.UsernameAlreadyExistException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(controllers = AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
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

    @Autowired
    private ObjectMapper objectMapper;
    private final static String USERNAME = "test@gmail.com";
    private final static String TOKEN_PARAM_NAME = "token";
    private final static String USER_NOT_FOUND_EXCEPTION_MSG =
            String.format("The user with username: %s does not exist!", USERNAME);
    private final static String MESSAGE_SENDING_EXCEPTION_MSG = "Failed to send the email";
    private final static String TOKEN_ALREADY_EXIST_EXCEPTION_MSG = "The valid confirmation token already exits!";
    private UserRegistrationDto userRegistrationDto;
    private ConfirmationTokenType activationTokenType;
    private ConfirmationTokenType resetPasswordTokenType;
    private String confirmToken;
    private ConfirmTokenDto confirmTokenDto;
    private UserAuthDto userAuthDto;
    private UsernameDto usernameDto;
    private AuthTokenDto authTokenDto;
    private ResetPasswordDto resetPasswordDto;
    private AccessAndRefreshTokensDto accessAndRefreshTokensDto;

    @BeforeEach
    public void setUp() {
        userRegistrationDto = UserRegistrationDto
                .builder()
                .firstname("Firstname")
                .lastname("Lastname")
                .email(USERNAME)
                .password("P@ssword123")
                .build();
        activationTokenType = ConfirmationTokenType.ACTIVATION;
        resetPasswordTokenType = ConfirmationTokenType.RESET_PASSWORD;
        confirmToken = "confirm-token";
        confirmTokenDto = ConfirmTokenDto.builder().token(confirmToken).build();
        userAuthDto = UserAuthDto.builder().email(USERNAME).password("password").build();
        usernameDto = UsernameDto.builder().email(USERNAME).build();
        resetPasswordDto = ResetPasswordDto.builder().password("P@ssword1234").build();
        accessAndRefreshTokensDto = AccessAndRefreshTokensDto
                .builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();
        authTokenDto = AuthTokenDto.builder().token("token").build();
    }

    @Test
    public void registerUser_ReturnsCreatedStatus() throws Exception {
        doNothing().when(userService).registerUser(userRegistrationDto);
        when(confirmationTokenService.createConfirmationToken(USERNAME, activationTokenType)).thenReturn(confirmTokenDto);
        doNothing().when(emailSenderService).sendEmail(USERNAME, confirmTokenDto.token(), activationTokenType);

        ResultActions response = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRegistrationDto)));

        verify(userService).registerUser(any());
        verify(confirmationTokenService).createConfirmationToken(userRegistrationDto.email(), activationTokenType);
        verify(emailSenderService).sendEmail(userRegistrationDto.email(), confirmTokenDto.token(),
                activationTokenType);

        response.andExpect(MockMvcResultMatchers.status().isCreated())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void registerUser_ThrowUsernameAlreadyExistException_ReturnsBadRequestStatusAndResponseStatusExceptionDto() throws Exception {
        String usernameAlreadyExistExceptionMsg = "This username already exist!";

        doThrow(new UsernameAlreadyExistException(usernameAlreadyExistExceptionMsg))
                .when(userService).registerUser(userRegistrationDto);

        ResultActions response = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRegistrationDto)));

        verify(confirmationTokenService, never()).createConfirmationToken(any(), any());
        verify(emailSenderService, never()).sendEmail(any(), any(), any());

        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.BAD_REQUEST,
                usernameAlreadyExistExceptionMsg);

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)));
    }

    @Test
    public void registerUser_ThrowMessageSendingException_ReturnsServiceUnavailableStatusAndResponseStatusExceptionDto() throws Exception {
        doNothing().when(userService).registerUser(userRegistrationDto);
        when(confirmationTokenService.createConfirmationToken(USERNAME, activationTokenType)).thenReturn(confirmTokenDto);
        doThrow(new MessageSendingException(MESSAGE_SENDING_EXCEPTION_MSG))
                .when(emailSenderService).sendEmail(any(), any(), any());

        ResultActions response = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRegistrationDto)));

        verify(userService).registerUser(any());
        verify(confirmationTokenService).createConfirmationToken(userRegistrationDto.email(), activationTokenType);

        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.SERVICE_UNAVAILABLE,
                MESSAGE_SENDING_EXCEPTION_MSG);

        response.andExpect(MockMvcResultMatchers.status().isServiceUnavailable())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)));
    }

    @Test
    public void activateAccount_ReturnsNoContentStatus() throws Exception {
        doNothing().when(confirmationTokenService).activateAccountUsingActivationToken(confirmToken);

        ResultActions response = mockMvc.perform(get("/api/v1/auth/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .param(TOKEN_PARAM_NAME, confirmToken));

        verify(confirmationTokenService).activateAccountUsingActivationToken(confirmToken);

        response.andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void activateAccount_ThrowInvalidConfirmationTokenException_ReturnsBadRequestStatusAndResponseStatusExceptionDto() throws Exception {
        String invalidConfirmationTokenExceptionMsg = "This confirmation token is invalid!";

        doThrow(new InvalidConfirmationTokenException(invalidConfirmationTokenExceptionMsg))
                .when(confirmationTokenService).activateAccountUsingActivationToken(confirmToken);

        ResultActions response = mockMvc.perform(get("/api/v1/auth/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .param(TOKEN_PARAM_NAME, confirmToken));

        ExceptionDto exceptionDto = getResponseStatusExceptionDto(HttpStatus.BAD_REQUEST,
                invalidConfirmationTokenExceptionMsg);

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(exceptionDto)));
    }

    @Test
    public void resendVerificationEmail_ReturnsNoContentStatus() throws Exception {
        when(confirmationTokenService.createConfirmationToken(USERNAME, activationTokenType))
                .thenReturn(confirmTokenDto);
        doNothing().when(emailSenderService).sendEmail(USERNAME, confirmToken, activationTokenType);

        ResultActions response = mockMvc.perform(post("/api/v1/auth/resend-verification-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usernameDto)));

        verify(confirmationTokenService).createConfirmationToken(USERNAME, activationTokenType);
        verify(emailSenderService).sendEmail(USERNAME, confirmToken, activationTokenType);

        response.andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void resendVerificationEmail_ThrowUserNotFoundException_ReturnsNotFoundStatusAndResponseStatusExceptionDto() throws Exception {
        doThrow(new UserNotFoundException(USER_NOT_FOUND_EXCEPTION_MSG)).when(confirmationTokenService)
                .createConfirmationToken(eq(USERNAME), eq(ConfirmationTokenType.ACTIVATION));

        ResultActions response = mockMvc.perform(post("/api/v1/auth/resend-verification-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usernameDto)));

        verify(emailSenderService, never()).sendEmail(any(), any(), any());

        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.NOT_FOUND,
                USER_NOT_FOUND_EXCEPTION_MSG);

        response.andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)));
    }

    @Test
    public void resendVerificationEmail_ThrowTokenAlreadyExistException_ReturnsBadRequestStatusAndResponseStatusExceptionDto() throws Exception {
        doThrow(new TokenAlreadyExistException(TOKEN_ALREADY_EXIST_EXCEPTION_MSG)).when(confirmationTokenService)
                .createConfirmationToken(eq(USERNAME), eq(ConfirmationTokenType.ACTIVATION));

        ResultActions response = mockMvc.perform(post("/api/v1/auth/resend-verification-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usernameDto)));

        verify(emailSenderService, never()).sendEmail(any(), any(), any());

        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.BAD_REQUEST,
                TOKEN_ALREADY_EXIST_EXCEPTION_MSG);

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)));
    }

    @Test
    public void resendVerificationEmail_ThrowMessageSendingException_ReturnsServiceUnavailableStatusAndResponseStatusExceptionDto() throws Exception {
        when(confirmationTokenService.createConfirmationToken(USERNAME, activationTokenType))
                .thenReturn(confirmTokenDto);
        doThrow(new MessageSendingException(MESSAGE_SENDING_EXCEPTION_MSG))
                .when(emailSenderService).sendEmail(any(), any(), any());

        ResultActions response = mockMvc.perform(post("/api/v1/auth/resend-verification-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usernameDto)));

        verify(confirmationTokenService).createConfirmationToken(USERNAME, activationTokenType);

        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.SERVICE_UNAVAILABLE,
                MESSAGE_SENDING_EXCEPTION_MSG);

        response.andExpect(MockMvcResultMatchers.status().isServiceUnavailable())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)));
    }

    @Test
    public void sendResetPasswordEmail_ReturnsNoContentStatus() throws Exception {
        when(confirmationTokenService.createConfirmationToken(USERNAME, resetPasswordTokenType))
                .thenReturn(confirmTokenDto);
        doNothing().when(emailSenderService).sendEmail(USERNAME, confirmToken, resetPasswordTokenType);

        ResultActions response = mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usernameDto)));

        verify(confirmationTokenService).createConfirmationToken(USERNAME, resetPasswordTokenType);
        verify(emailSenderService).sendEmail(USERNAME, confirmToken, resetPasswordTokenType);

        response.andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void sendResetPasswordEmail_ThrowUserNotFoundException_ReturnsNotFoundStatusAndResponseStatusExceptionDto() throws Exception {
        doThrow(new UserNotFoundException(USER_NOT_FOUND_EXCEPTION_MSG))
                .when(confirmationTokenService).createConfirmationToken(USERNAME, resetPasswordTokenType);

        ResultActions response = mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usernameDto)));

        verify(emailSenderService, never()).sendEmail(any(), any(), any());

        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.NOT_FOUND,
                USER_NOT_FOUND_EXCEPTION_MSG);

        response.andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)));
    }

    @Test
    public void sendResetPasswordEmail_ThrowUserNotFoundException_ReturnsBadRequestStatusAndResponseStatusExceptionDto() throws Exception {
        doThrow(new TokenAlreadyExistException(TOKEN_ALREADY_EXIST_EXCEPTION_MSG))
                .when(confirmationTokenService).createConfirmationToken(USERNAME, resetPasswordTokenType);

        ResultActions response = mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usernameDto)));

        verify(emailSenderService, never()).sendEmail(any(), any(), any());

        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.BAD_REQUEST,
                TOKEN_ALREADY_EXIST_EXCEPTION_MSG);

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)));
    }

    @Test
    public void sendResetPasswordEmail_ThrowMessageSendingException_ReturnsServiceUnavailableStatusAndResponseStatusExceptionDto() throws Exception {
        when(confirmationTokenService.createConfirmationToken(USERNAME, resetPasswordTokenType))
                .thenReturn(confirmTokenDto);
        doThrow(new MessageSendingException(MESSAGE_SENDING_EXCEPTION_MSG))
                .when(emailSenderService).sendEmail(any(), any(), any());

        ResultActions response = mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usernameDto)));

        verify(confirmationTokenService).createConfirmationToken(USERNAME, resetPasswordTokenType);

        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.SERVICE_UNAVAILABLE,
                MESSAGE_SENDING_EXCEPTION_MSG);

        response.andExpect(MockMvcResultMatchers.status().isServiceUnavailable())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)));
    }

    @Test
    public void changePassword_ReturnsNoContentStatus() throws Exception {
        doNothing().when(confirmationTokenService).changePasswordUsingResetToken(confirmToken, resetPasswordDto);

        ResultActions response = mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .param("token", confirmToken)
                .content(objectMapper.writeValueAsString(resetPasswordDto)));

        verify(confirmationTokenService).changePasswordUsingResetToken(any(), any());

        response.andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void changePassword_ThrowInvalidConfirmationTokenException_ReturnsBadRequestStatusAndResponseStatusExceptionDto() throws Exception {
        String invalidConfirmationTokenExceptionMsg = "The reset password token is invalid!";

        doThrow(new InvalidConfirmationTokenException(invalidConfirmationTokenExceptionMsg))
                .when(confirmationTokenService).changePasswordUsingResetToken(eq(confirmToken), any());

        ResultActions response = mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .param("token", confirmToken)
                .content(objectMapper.writeValueAsString(resetPasswordDto)));

        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.BAD_REQUEST,
                invalidConfirmationTokenExceptionMsg);

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void authenticate_ReturnsOkStatusAndAccessAndRefreshTokensDto() throws Exception {
        when(userService.authenticateUser(userAuthDto)).thenReturn(accessAndRefreshTokensDto);

        ResultActions response = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userAuthDto)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(accessAndRefreshTokensDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void authenticate_ThrowUserNotFoundException_ReturnsBadRequestStatusAndAccessAndRefreshTokensDto() throws Exception {
        doThrow(new UserNotFoundException(USER_NOT_FOUND_EXCEPTION_MSG))
                .when(userService).authenticateUser(userAuthDto);

        ResultActions response = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userAuthDto)));

        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.BAD_REQUEST,
                USER_NOT_FOUND_EXCEPTION_MSG);

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void refreshToken_ReturnsCreatedStatusAndAuthTokenDto() throws Exception {
        when(jwtTokenService.refreshAccessToken(authTokenDto)).thenReturn(authTokenDto);

        ResultActions response = mockMvc.perform(post("/api/v1/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authTokenDto)));

        verify(jwtTokenService).refreshAccessToken(any());

        response.andExpect(MockMvcResultMatchers.status().isCreated())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void refreshToken_ThrowTokenAlreadyExistException_ReturnsBadRequestStatusAndAuthTokenDto() throws Exception {
        doThrow(new TokenAlreadyExistException(TOKEN_ALREADY_EXIST_EXCEPTION_MSG))
                .when(jwtTokenService).refreshAccessToken(authTokenDto);

        ResultActions response = mockMvc.perform(post("/api/v1/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authTokenDto)));

        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.BAD_REQUEST,
                TOKEN_ALREADY_EXIST_EXCEPTION_MSG);

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    private ExceptionDto getResponseStatusExceptionDto(HttpStatus httpStatus, String msg) {
        return ExceptionDto
                .builder()
                .error(httpStatus.name())
                .status(httpStatus.value())
                .message(msg)
                .build();
    }
}