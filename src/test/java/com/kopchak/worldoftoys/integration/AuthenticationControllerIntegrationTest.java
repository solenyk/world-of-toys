package com.kopchak.worldoftoys.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.kopchak.worldoftoys.dto.error.ResponseStatusExceptionDto;
import com.kopchak.worldoftoys.dto.token.AuthTokenDto;
import com.kopchak.worldoftoys.dto.user.ResetPasswordDto;
import com.kopchak.worldoftoys.dto.user.UserAuthDto;
import com.kopchak.worldoftoys.dto.user.UserRegistrationDto;
import com.kopchak.worldoftoys.dto.user.UsernameDto;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@Slf4j
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

    private final static String REGISTERED_ACTIVATED_USERNAME = "john.doe@example.com";
    private final static String REGISTERED_NOT_ACTIVATED_USERNAME = "alice.johnson@example.com";
    private final static String NOT_REGISTERED_USERNAME = "not_registered_user@example.com";
    private final static String VALID_PASSWORD = "P@ssword123";
    private String accountActivationSubject;
    private String activationConfirmToken;
    private String resetPasswordConfirmToken;
    private UsernameDto registeredNotActivatedUserDto;
    private UsernameDto notRegisteredUserDto;
    private ResetPasswordDto validResetPasswordDto;

    @BeforeEach
    public void setUp() {
        accountActivationSubject = "Confirm your email";
        activationConfirmToken = "8e5648d7-9b4e-4724-83a1-be7e64603e48";
        resetPasswordConfirmToken = "8e5648d7-9b4e-4724-83a1-be7e64603e47";
        registeredNotActivatedUserDto = UsernameDto.builder().email(REGISTERED_NOT_ACTIVATED_USERNAME).build();
        notRegisteredUserDto = UsernameDto.builder().email(NOT_REGISTERED_USERNAME).build();
        validResetPasswordDto = ResetPasswordDto.builder().password("NewP@ssword123").build();
    }

    @Test
    public void registerUser_NotRegisteredUser_ReturnsCreatedStatus() throws Exception {
        UserRegistrationDto userRegistrationDto = UserRegistrationDto
                .builder()
                .firstname("Firstname")
                .lastname("Lastname")
                .email(NOT_REGISTERED_USERNAME)
                .password(VALID_PASSWORD)
                .build();

        ResultActions response = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRegistrationDto)));

        response.andExpect(MockMvcResultMatchers.status().isCreated())
                .andDo(MockMvcResultHandlers.print());

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages.length).isEqualTo(1);

        MimeMessage receivedMessage = receivedMessages[0];
        assertThat(receivedMessage.getSubject()).isEqualTo(accountActivationSubject);
    }

    @Test
    public void registerUser_RegisteredUser_ReturnsBadRequestStatusAndResponseStatusExceptionDto() throws Exception {
        UserRegistrationDto userRegistrationDto = UserRegistrationDto
                .builder()
                .firstname("Firstname")
                .lastname("Lastname")
                .email("john.doe@example.com")
                .password(VALID_PASSWORD)
                .build();

        ResultActions response = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRegistrationDto)));

        ResponseStatusExceptionDto responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.BAD_REQUEST,
                "This username already exist!");

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)));

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
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
    public void activateAccount_InvalidConfirmToken_ReturnsBadRequestStatusAndResponseStatusExceptionDto() throws Exception {
        String invalidConfirmToken = "invalid-confirm-token";

        ResultActions response = mockMvc.perform(get("/api/v1/auth/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .param("token", invalidConfirmToken));

        ResponseStatusExceptionDto responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.BAD_REQUEST,
                "This confirmation token is invalid!");

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)));

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages.length).isEqualTo(0);
    }

    @Test
    public void resendVerificationEmail_RegisteredAndNotActivatedUser_ReturnsNoContentStatus() throws Exception {
        ResultActions response = mockMvc.perform(post("/api/v1/auth/resend-verification-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registeredNotActivatedUserDto)));

        response.andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages.length).isEqualTo(1);

        MimeMessage receivedMessage = receivedMessages[0];
        assertThat(receivedMessage.getSubject()).isEqualTo(accountActivationSubject);
    }


    @Test
    public void resendVerificationEmail_NotRegisteredUser_ReturnsNotFoundStatusAndResponseStatusExceptionDto() throws Exception {
        ResultActions response = mockMvc.perform(post("/api/v1/auth/resend-verification-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notRegisteredUserDto)));

        ResponseStatusExceptionDto responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.NOT_FOUND,
                "User with this username does not exist!");

        response.andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)));

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages.length).isEqualTo(0);
    }

    @Test
    public void resendVerificationEmail_RegisteredAndActivatedUser_ReturnsConflictStatusAndResponseStatusExceptionDto() throws Exception {
        UsernameDto registeredAndActivatedUserDto = UsernameDto.builder().email(REGISTERED_ACTIVATED_USERNAME).build();

        ResultActions response = mockMvc.perform(post("/api/v1/auth/resend-verification-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registeredAndActivatedUserDto)));

        ResponseStatusExceptionDto responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.CONFLICT,
                "Account is already activated!");

        response.andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)));

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages.length).isEqualTo(0);
    }

    @Test
    public void sendResetPasswordEmail_RegisteredUser_ReturnsNoContentStatus() throws Exception {
        String passwordResetSubject = "Reset your password";
        ResultActions response = mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registeredNotActivatedUserDto)));

        response.andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages.length).isEqualTo(1);

        MimeMessage receivedMessage = receivedMessages[0];
        assertThat(receivedMessage.getSubject()).isEqualTo(passwordResetSubject);
    }

    @Test
    public void sendResetPasswordEmail_NotRegisteredUser_ReturnsNotFoundStatusAndResponseStatusExceptionDto() throws Exception {
        ResultActions response = mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notRegisteredUserDto)));

        ResponseStatusExceptionDto responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.NOT_FOUND,
                "User with this username does not exist!");

        response.andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)));

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
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
    public void changePassword_ValidConfirmTokenAndNewPasswordMatchOldPassword_ReturnsBadRequestStatusAndResponseStatusExceptionDto() throws Exception {
        ResetPasswordDto invalidResetPasswordDto = ResetPasswordDto.builder().password(VALID_PASSWORD).build();

        ResultActions response = mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .param("token", resetPasswordConfirmToken)
                .content(objectMapper.writeValueAsString(invalidResetPasswordDto)));

        ResponseStatusExceptionDto responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.BAD_REQUEST,
                "New password matches old password!");

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void changePassword_InvalidConfirmToken_ReturnsBadRequestStatusAndResponseStatusExceptionDto() throws Exception {
        ResultActions response = mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .param("token", activationConfirmToken)
                .content(objectMapper.writeValueAsString(validResetPasswordDto)));

        ResponseStatusExceptionDto responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.BAD_REQUEST,
                "This confirmation token is invalid!");

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void authenticate_RegisteredAndActivatedUser_ReturnsOkStatusAndAccessAndRefreshTokensDto() throws Exception {
        UserAuthDto registeredAndActivatedUserAuthDto = UserAuthDto.builder().email(REGISTERED_ACTIVATED_USERNAME)
                .password(VALID_PASSWORD).build();

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
    public void authenticate_RegisteredAndNotActivatedUser_ReturnsForbiddenStatusAndResponseStatusExceptionDto() throws Exception {
        UserAuthDto registeredAndNotActivatedUserAuthDto = UserAuthDto.builder().email(REGISTERED_NOT_ACTIVATED_USERNAME)
                .password(VALID_PASSWORD).build();

        ResultActions response = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registeredAndNotActivatedUserAuthDto)));

        ResponseStatusExceptionDto responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.FORBIDDEN,
                "Account is not activated!");

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)));
    }

    @Test
    public void authenticate_NotRegisteredUser_ReturnsUnauthorizedStatusAndResponseStatusExceptionDto() throws Exception {
        UserAuthDto notRegisteredUserAuthDto = UserAuthDto
                .builder()
                .email(NOT_REGISTERED_USERNAME)
                .password("password")
                .build();

        ResultActions response = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notRegisteredUserAuthDto)));

        ResponseStatusExceptionDto responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.UNAUTHORIZED,
                "Bad user credentials!");

        response.andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)));
    }

    @Test
    public void refreshToken_ValidTokenAndActiveAuthTokenNotExists_ReturnsCreatedStatusAndAuthTokenDto() throws Exception {
        AuthTokenDto validAuthTokenDto = AuthTokenDto
                .builder()
                .token("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGljZS5qb2huc29uQGV4YW1wbGUuY29tIiwiaWF0IjoxNjk2NDI2ODI" +
                        "yLCJleHAiOjEwMzM2NDI2ODIyfQ.K8ACIiQKVcSr5IX_snOX-WXNebx2-FMIQP4gj4Qg_Pk")
                .build();
        long expTokenTimeInSeconds = 10336426822L;
        logTokenExpirationDate(expTokenTimeInSeconds);

        ResultActions response = mockMvc.perform(post("/api/v1/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAuthTokenDto)));

        response.andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void refreshToken_ValidTokenAndActiveAuthTokenExists_ReturnsBadRequestStatusAndResponseStatusExceptionDto() throws Exception {
        AuthTokenDto validAuthTokenDto = AuthTokenDto
                .builder()
                .token("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTY5NjQyNjgyMSwi" +
                        "ZXhwIjoxMDMzNjQyNjgyMX0.jeeuTzGgKrixMp6_dizMNLicp6n0gwECAId-ATLqbns")
                .build();
        long expTokenTimeInSeconds = 10336426821L;
        logTokenExpirationDate(expTokenTimeInSeconds);

        ResultActions response = mockMvc.perform(post("/api/v1/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAuthTokenDto)));

        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.BAD_REQUEST,
                "There is valid access token!");

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)));
    }

    @Test
    public void refreshToken_InvalidToken_ReturnsBadRequestStatusAndResponseStatusExceptionDto() throws Exception {
        AuthTokenDto invalidAuthTokenDto = AuthTokenDto
                .builder()
                .token("invalid-token")
                .build();

        ResultActions response = mockMvc.perform(post("/api/v1/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidAuthTokenDto)));

        ResponseStatusExceptionDto responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.BAD_REQUEST,
                "This refresh token is invalid!");

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)));
    }

    private ResponseStatusExceptionDto getResponseStatusExceptionDto(HttpStatus httpStatus, String msg) {
        return ResponseStatusExceptionDto
                .builder()
                .error(httpStatus.name())
                .status(httpStatus.value())
                .message(msg)
                .build();
    }

    private void logTokenExpirationDate(long expTokenTimeInSeconds) {
        Instant instant = Instant.ofEpochSecond(expTokenTimeInSeconds);
        LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        log.warn("Token expiration date is {}", localDateTime);
    }
}