package com.kopchak.worldoftoys.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.kopchak.worldoftoys.dto.error.ErrorResponseDto;
import com.kopchak.worldoftoys.dto.user.ResetPasswordDto;
import com.kopchak.worldoftoys.dto.user.UserAuthDto;
import com.kopchak.worldoftoys.dto.user.UserRegistrationDto;
import com.kopchak.worldoftoys.dto.user.UsernameDto;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("integrationtest")
class AuthenticationControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @RegisterExtension
    public static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("test", "password"))
            .withPerMethodLifecycle(true);

    @Autowired
    private ObjectMapper objectMapper;

    private String accountActivationSubject;
    private String passwordResetSubject;
    private String activationConfirmToken;
    private String resetPasswordConfirmToken;
    private String invalidConfirmToken;
    private UsernameDto registeredNotActivatedUserDto;
    private UsernameDto notRegisteredUserDto;
    private UsernameDto registeredAndActivatedUserDto;
    private ResetPasswordDto validResetPasswordDto;
    private ResetPasswordDto invalidResetPasswordDto;
    private UserAuthDto registeredAndActivatedUserAuthDto;
    private UserAuthDto registeredAndNotActivatedUserAuthDto;
    private UserAuthDto notRegisteredUserAuthDto;
    private UserRegistrationDto userRegistrationDto;

    @BeforeEach
    public void setUp(){
        accountActivationSubject = "Confirm your email";
        passwordResetSubject = "Reset your password";
        activationConfirmToken = "8e5648d7-9b4e-4724-83a1-be7e64603e48";
        resetPasswordConfirmToken = "8e5648d7-9b4e-4724-83a1-be7e64603e47";
        invalidConfirmToken = "invalid-confirm-token";
        registeredNotActivatedUserDto = UsernameDto.builder().email("alice.johnson@example.com").build();
        notRegisteredUserDto = UsernameDto.builder().email("non-existing-user@example.com").build();
        registeredAndActivatedUserDto = UsernameDto.builder().email("john.doe@example.com").build();
        validResetPasswordDto = ResetPasswordDto.builder().password("new-password").build();
        invalidResetPasswordDto = ResetPasswordDto.builder().password("password").build();
        registeredAndActivatedUserAuthDto = UserAuthDto
                .builder()
                .email("john.doe@example.com")
                .password("password")
                .build();
        registeredAndNotActivatedUserAuthDto = UserAuthDto
                .builder()
                .email("alice.johnson@example.com")
                .password("password")
                .build();
        notRegisteredUserAuthDto = UserAuthDto
                .builder()
                .email("non-existing-user@example.com")
                .password("password")
                .build();
        userRegistrationDto = UserRegistrationDto
                .builder()
                .firstname("Firstname")
                .lastname("Lastname")
                .email("test@gmail.com")
                .password("password")
                .build();
    }

    @Test
    public void registerUser_NotRegisteredUser_ReturnsCreatedStatus() throws Exception {
        ResultActions response = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRegistrationDto)));

        response.andExpect(MockMvcResultMatchers.status().isCreated())
                .andDo(MockMvcResultHandlers.print());

        MimeMessage [] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages.length).isEqualTo(1);

        MimeMessage receivedMessage = receivedMessages[0];
        assertThat(receivedMessage.getSubject()).isEqualTo(accountActivationSubject);
    }

    @Test
    public void registerUser_RegisteredUser_ReturnsBadRequestStatusAndErrorResponseDto() throws Exception {
        UserRegistrationDto userRegistrationDto = UserRegistrationDto
                .builder()
                .firstname("Firstname")
                .lastname("Lastname")
                .email("john.doe@example.com")
                .password("password")
                .build();

        ResultActions response = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRegistrationDto)));

        ErrorResponseDto errorResponseDto = getErrorResponseDto(HttpStatus.BAD_REQUEST, "This username already exist!");

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(errorResponseDto)));

        MimeMessage [] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages.length).isEqualTo(0);
    }

    @Test
    public void activateAccount_ValidConfirmToken_ReturnsNoContentStatus() throws Exception {
        ResultActions response = mockMvc.perform(get("/api/v1/auth/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .param("token", activationConfirmToken));

        response.andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void activateAccount_InvalidConfirmToken_ReturnsBadRequestStatusAndErrorResponseDto() throws Exception {
        ResultActions response = mockMvc.perform(get("/api/v1/auth/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .param("token", invalidConfirmToken));

        ErrorResponseDto errorResponseDto = getErrorResponseDto(HttpStatus.BAD_REQUEST, "This confirmation token is invalid!");

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(errorResponseDto)));

        MimeMessage [] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages.length).isEqualTo(0);
    }

    @Test
    public void resendVerificationEmail_RegisteredAndNotActivatedUser_ReturnsNoContentStatus() throws Exception {
        ResultActions response = mockMvc.perform(post("/api/v1/auth/resend-verification-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registeredNotActivatedUserDto)));

        response.andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());

        MimeMessage [] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages.length).isEqualTo(1);

        MimeMessage receivedMessage = receivedMessages[0];
        assertThat(receivedMessage.getSubject()).isEqualTo(accountActivationSubject);
    }


    @Test
    public void resendVerificationEmail_NotRegisteredUser_ReturnsNotFoundStatusAndErrorResponseDto() throws Exception {
        ResultActions response = mockMvc.perform(post("/api/v1/auth/resend-verification-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notRegisteredUserDto)));

        ErrorResponseDto errorResponseDto = getErrorResponseDto(HttpStatus.NOT_FOUND, "User with this username does not exist!");

        response.andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().json(objectMapper.writeValueAsString(errorResponseDto)));

        MimeMessage [] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages.length).isEqualTo(0);
    }

    @Test
    public void resendVerificationEmail_RegisteredAndActivatedUser_ReturnsConflictStatusAndErrorResponseDto() throws Exception {
        ResultActions response = mockMvc.perform(post("/api/v1/auth/resend-verification-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registeredAndActivatedUserDto)));

        ErrorResponseDto errorResponseDto = getErrorResponseDto(HttpStatus.CONFLICT, "Account is already activated!");

        response.andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(content().json(objectMapper.writeValueAsString(errorResponseDto)));

        MimeMessage [] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages.length).isEqualTo(0);
    }

    @Test
    public void sendResetPasswordEmail_RegisteredUser_ReturnsNoContentStatus() throws Exception {
        ResultActions response = mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registeredNotActivatedUserDto)));

        response.andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());

        MimeMessage [] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages.length).isEqualTo(1);

        MimeMessage receivedMessage = receivedMessages[0];
        assertThat(receivedMessage.getSubject()).isEqualTo(passwordResetSubject);
    }

    @Test
    public void sendResetPasswordEmail_NotRegisteredUser_ReturnsNotFoundStatusAndErrorResponseDto() throws Exception {
        ResultActions response = mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notRegisteredUserDto)));

        ErrorResponseDto errorResponseDto = getErrorResponseDto(HttpStatus.NOT_FOUND, "User with this username does not exist!");

        response.andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().json(objectMapper.writeValueAsString(errorResponseDto)));

        MimeMessage [] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages.length).isEqualTo(0);
    }

    @Test
    public void changePassword_ValidConfirmTokenAndNewPasswordNotMatchOldPassword_ReturnsNoContentStatus() throws Exception {
        ResultActions response = mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .param("token", resetPasswordConfirmToken)
                .content(objectMapper.writeValueAsString(validResetPasswordDto)));

        response.andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void changePassword_ValidConfirmTokenAndNewPasswordMatchOldPassword_ReturnsBadRequestStatusAndErrorResponseDto() throws Exception {
        ResultActions response = mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .param("token", resetPasswordConfirmToken)
                .content(objectMapper.writeValueAsString(invalidResetPasswordDto)));

        ErrorResponseDto errorResponseDto = getErrorResponseDto(HttpStatus.BAD_REQUEST, "New password matches old password!");

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(errorResponseDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void changePassword_InvalidConfirmToken_ReturnsBadRequestStatusAndErrorResponseDto() throws Exception {
        ResultActions response = mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .param("token", activationConfirmToken)
                .content(objectMapper.writeValueAsString(validResetPasswordDto)));

        ErrorResponseDto errorResponseDto = getErrorResponseDto(HttpStatus.BAD_REQUEST, "This confirmation token is invalid!");

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(errorResponseDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void authenticate_RegisteredAndActivatedUser_ReturnsOkStatusAndAccessAndRefreshTokensDto() throws Exception {
        ResultActions response = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registeredAndActivatedUserAuthDto)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").exists())
                .andDo(MockMvcResultHandlers.print());

        assertThat(MockMvcResultMatchers.content()).isNotNull();
    }

    @Test
    public void authenticate_RegisteredAndNotActivatedUser_ReturnsForbiddenStatusAndErrorResponseDto() throws Exception {
        ResultActions response = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registeredAndNotActivatedUserAuthDto)));

        ErrorResponseDto errorResponseDto = getErrorResponseDto(HttpStatus.FORBIDDEN, "Account is not activated!");

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().json(objectMapper.writeValueAsString(errorResponseDto)));
    }

    @Test
    public void authenticate_NotRegisteredUser_ReturnsUnauthorizedStatusAndErrorResponseDto() throws Exception {
        ResultActions response = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notRegisteredUserAuthDto)));

        ErrorResponseDto errorResponseDto = getErrorResponseDto(HttpStatus.UNAUTHORIZED, "Bad user credentials!");

        response.andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().json(objectMapper.writeValueAsString(errorResponseDto)));
    }
    private ErrorResponseDto getErrorResponseDto(HttpStatus httpStatus, String msg) {
        return ErrorResponseDto
                .builder()
                .error(httpStatus.name())
                .status(httpStatus.value())
                .message(msg)
                .build();
    }
}