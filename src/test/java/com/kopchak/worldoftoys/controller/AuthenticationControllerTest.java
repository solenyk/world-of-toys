package com.kopchak.worldoftoys.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kopchak.worldoftoys.dto.error.ErrorResponseDto;
import com.kopchak.worldoftoys.dto.token.ConfirmTokenDto;
import com.kopchak.worldoftoys.dto.user.UserRegistrationDto;
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
    private ConfirmTokenDto confirmTokenDto;
    private ResponseStatusException usernameAlreadyExistException;

    @BeforeEach
    public void setUp() {
        userRegistrationDto = UserRegistrationDto
                .builder()
                .firstname("Firstname")
                .lastname("Lastname")
                .email("test@gmail.com")
                .password("password")
                .build();
        activationTokenType = ConfirmationTokenType.ACTIVATION;
        confirmTokenDto = ConfirmTokenDto
                .builder()
                .token("confirm-token")
                .build();
        usernameAlreadyExistException = new UsernameAlreadyExistException(HttpStatus.BAD_REQUEST, "This username already exist!");
    }

    @Test
    public void registerUser_UserRegistrationDtoWithUnregisteredUserEmail_ReturnsCreatedStatus() throws Exception {
        when(userService.isUserRegistered(userRegistrationDto.getEmail())).thenReturn(false);
        doNothing().when(userService).registerUser(userRegistrationDto);
        when(confirmationTokenService.createConfirmationToken(userRegistrationDto.getEmail(), activationTokenType))
                .thenReturn(confirmTokenDto);
        doNothing().when(emailSenderService).sendEmail(userRegistrationDto.getEmail(), confirmTokenDto.getToken(),
                activationTokenType);

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