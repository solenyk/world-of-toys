package com.kopchak.worldoftoys.service.impl;

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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailSenderServiceImplTest {
    @Mock
    private JavaMailSender mailSender;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TemplateEngine templateEngine;
    @Mock
    private HttpServletRequest request;

    private MimeMessage mimeMessage;
    @Mock
    private MimeMessageHelper helper;

    @InjectMocks
    private EmailSenderServiceImpl emailSenderService;

    @BeforeEach
    public void before() {
        mimeMessage = new MimeMessage((Session)null);
    }

    @Test
    public void send_ValidEmail_SuccessfulSend() throws MessagingException, IOException {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        String emailRecipient = "recipient@example.com";
        String emailContent = "Test email content";
        String msgSubject = "Test Subject";

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

        String emailRecipient = "recipient@example.com";
        String emailContent = "Test email content";
        String msgSubject = "Test Subject";
        doAnswer(invocation -> {
            throw new MessagingException("Simulated email sending failure");
        }).when(mailSender).send(mimeMessage);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                emailSenderService.send(emailRecipient, emailContent, msgSubject));

        String expectedMessage = "Failed to send email";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

}