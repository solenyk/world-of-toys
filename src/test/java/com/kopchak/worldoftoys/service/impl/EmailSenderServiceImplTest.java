package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.domain.token.ConfirmationTokenType;
import com.kopchak.worldoftoys.domain.user.AppUser;
import com.kopchak.worldoftoys.repository.user.UserRepository;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailSenderServiceImplTest {
    @Mock
    private JavaMailSender mailSender;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ITemplateEngine templateEngine;
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private EmailSenderServiceImpl emailSenderService;

    private MimeMessage mimeMessage;
    private String emailRecipient;
    private String emailContent;
    private String msgSubject;
    private String userEmail;
    private String confirmToken;
    private ConfirmationTokenType resetPasswordTokenType;

    @BeforeEach
    void setUp() {
        mimeMessage = new MimeMessage((Session) null);
        emailRecipient = "recipient@example.com";
        emailContent = "Test email content";
        msgSubject = "Test Subject";
        userEmail = "user@example.com";
        confirmToken = "testConfirmationToken";
        resetPasswordTokenType = ConfirmationTokenType.RESET_PASSWORD;
    }

    @Test
    public void send_ValidEmail_SuccessfulSend() throws MessagingException, IOException {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailSenderService.send(emailRecipient, emailContent, msgSubject);
        String actualRecipient = mimeMessage.getRecipients(Message.RecipientType.TO)[0].toString();
        String actualSubject = mimeMessage.getSubject();
        String actualEmailContent = mimeMessage.getContent().toString();

        assertEquals(emailRecipient, actualRecipient);
        assertEquals(emailContent, actualEmailContent);
        assertEquals(msgSubject, actualSubject);
    }

    @Test
    public void send_InvalidEmail_ThrowsIllegalStateException() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doAnswer(invocation -> {
            throw new MessagingException("Simulated email sending failure");
        }).when(mailSender).send(mimeMessage);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                emailSenderService.send(emailRecipient, emailContent, msgSubject));
        String expectedMessage = "Failed to send email";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void sendEmail_ActivationToken_SuccessfulSend(){
        mockMethodsCallForSendEmail();

        emailSenderService.sendEmail(userEmail, confirmToken, ConfirmationTokenType.ACTIVATION);

        verify(templateEngine).process(eq("email-template"), any(Context.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    public void sendEmail_ResetPasswordToken_SuccessfulSend(){
        mockMethodsCallForSendEmail();

        emailSenderService.sendEmail(userEmail, confirmToken, resetPasswordTokenType);

        verify(templateEngine).process(eq("email-template"), any(Context.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    public void sendEmail_ResetPasswordToken_ThrowsUserNotFoundException(){
        ResponseStatusException exception = assertThrows(UserNotFoundException1.class, () ->
                emailSenderService.sendEmail(userEmail, confirmToken, resetPasswordTokenType));

        String expectedMessage = "User with this username does not exist!";
        String actualMessage = exception.getReason();
        int expectedStatusCode = HttpStatus.NOT_FOUND.value();
        int actualStatusCode = exception.getStatusCode().value();

        assertEquals(expectedMessage, actualMessage);
        assertEquals(expectedStatusCode, actualStatusCode);
    }

    private void mockMethodsCallForSendEmail(){
        AppUser user = AppUser
                .builder()
                .firstname("Firstname")
                .lastname("Lastname")
                .build();
        String requestUrl = "Test-string-request-url";
        String servletPath = "Test-servlet-path";
        String expectedEmailContent = "Expected Email Content";

        when(userRepository.findByEmail(userEmail)).thenReturn(java.util.Optional.of(user));
        when(request.getRequestURL()).thenReturn(new StringBuffer(requestUrl));
        when(request.getServletPath()).thenReturn(servletPath);
        when(templateEngine.process(eq("email-template"), any(Context.class))).thenReturn(expectedEmailContent);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }
}