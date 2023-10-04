package com.kopchak.worldoftoys.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.kopchak.worldoftoys.dto.error.ErrorResponseDto;
import com.kopchak.worldoftoys.dto.user.UserRegistrationDto;
import com.kopchak.worldoftoys.dto.user.UsernameDto;
import com.kopchak.worldoftoys.service.ConfirmationTokenService;
import com.kopchak.worldoftoys.service.EmailSenderService;
import com.kopchak.worldoftoys.service.UserService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("integrationtest")
class AuthenticationControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;
    @Autowired
    private ConfirmationTokenService confirmationTokenService;
    @Autowired
    private EmailSenderService emailSenderService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @RegisterExtension
    public static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("test", "password"))
            .withPerMethodLifecycle(true);

    @Autowired
    private ObjectMapper objectMapper;

    private String accountActivationSubject;
    private String confirmToken;
    private String invalidConfirmToken;
    private UsernameDto registeredNotActivatedUserDto;
    private UsernameDto notRegisteredUserDto;
    private UsernameDto registeredAndActivatedUserDto;

    @BeforeEach
    public void setUp(){
        accountActivationSubject = "Confirm your email";
        confirmToken = "8e5648d7-9b4e-4724-83a1-be7e64603e48";
        invalidConfirmToken = "invalid-confirm-token";
        registeredNotActivatedUserDto = UsernameDto.builder().email("alice.johnson@example.com").build();
        notRegisteredUserDto = UsernameDto.builder().email("non-existing-user@example.com").build();
        registeredAndActivatedUserDto = UsernameDto.builder().email("john.doe@example.com").build();
    }

    @Test
    public void registerUser_NotRegisteredUser_ReturnsCreatedStatus() throws Exception {
        UserRegistrationDto userRegistrationDto = UserRegistrationDto
                .builder()
                .firstname("Firstname")
                .lastname("Lastname")
                .email("test@gmail.com")
                .password("password")
                .build();

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
                .param("token", confirmToken));

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

    private ErrorResponseDto getErrorResponseDto(HttpStatus httpStatus, String msg) {
        return ErrorResponseDto
                .builder()
                .error(httpStatus.name())
                .status(httpStatus.value())
                .message(msg)
                .build();
    }
}