package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.domain.order.OrderStatus;
import com.kopchak.worldoftoys.domain.order.payment.PaymentStatus;
import com.kopchak.worldoftoys.domain.token.ConfirmationTokenType;
import com.kopchak.worldoftoys.domain.user.AppUser;
import com.kopchak.worldoftoys.exception.MessageSendingException;
import com.kopchak.worldoftoys.exception.UserNotFoundException;
import com.kopchak.worldoftoys.repository.user.UserRepository;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
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

    @InjectMocks
    private EmailSenderServiceImpl emailSenderService;

    private static final String FIRSTNAME = "Firstname";
    private static final String ORDER_ID = "1";

    private AppUser user;
    private MimeMessage mimeMessage;
    private String emailRecipient;
    private String emailContent;
    private String msgSubject;
    private String userEmail;
    private String confirmToken;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
        user = AppUser
                .builder()
                .firstname(FIRSTNAME)
                .lastname("Lastname")
                .build();
        mimeMessage = new MimeMessage((Session) null);
        emailRecipient = "recipient@example.com";
        emailContent = "Test email content";
        msgSubject = "Test Subject";
        userEmail = "user@example.com";
        confirmToken = "testConfirmationToken";
    }

    @Test
    public void send_ValidEmail_SuccessfulSend() throws MessageSendingException, MessagingException, IOException {
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
    public void send_InvalidEmail_ThrowsMessageSendingException() {
        String messagingExceptionMsg = "Simulated email sending failure";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doAnswer(invocation -> {
            throw new MessagingException(messagingExceptionMsg);
        }).when(mailSender).send(mimeMessage);

        String expectedMessage = String.format("Failed to send the email: %s", messagingExceptionMsg);

        assertException(MessageSendingException.class, expectedMessage,
                () -> emailSenderService.send(emailRecipient, emailContent, msgSubject));

    }

    @Test
    public void sendEmail_ActivationTokenType() throws UserNotFoundException, MessageSendingException {
        when(userRepository.findByEmail(userEmail)).thenReturn(java.util.Optional.of(user));
        mockEmailProcessing();

        emailSenderService.sendEmail(userEmail, confirmToken, ConfirmationTokenType.ACTIVATION);

        verify(templateEngine).process(eq("email"), any(Context.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    public void sendEmail_ResetPasswordTokenType() throws UserNotFoundException, MessageSendingException {
        when(userRepository.findByEmail(userEmail)).thenReturn(java.util.Optional.of(user));
        mockEmailProcessing();

        emailSenderService.sendEmail(userEmail, confirmToken, ConfirmationTokenType.RESET_PASSWORD);

        verify(templateEngine).process(eq("email"), any(Context.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    public void sendEmail_NonExistentUser_ThrowsUserNotFoundException() {
        String userNotFoundExceptionMsg = String.format("The user with username: %s does not exist!", userEmail);
        assertException(UserNotFoundException.class, userNotFoundExceptionMsg,
                () -> emailSenderService.sendEmail(userEmail, confirmToken, ConfirmationTokenType.RESET_PASSWORD));
    }

    @Test
    public void sendEmail_PaymentStatus() throws MessageSendingException {
        PaymentStatus paymentStatus = PaymentStatus.COMPLETE;
        mockEmailProcessing();

        emailSenderService.sendEmail(userEmail, FIRSTNAME, ORDER_ID, paymentStatus);

        verify(templateEngine).process(eq("email"), any(Context.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    public void sendEmail_OrderStatus() throws MessageSendingException {
        OrderStatus orderStatus = OrderStatus.CANCELED;
        mockEmailProcessing();

        emailSenderService.sendEmail(userEmail, FIRSTNAME, ORDER_ID, orderStatus);

        verify(templateEngine).process(eq("email"), any(Context.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    private void assertException(Class<? extends Exception> expectedExceptionType,
                                 String expectedMessage, Executable executable) {
        Exception exception = assertThrows(expectedExceptionType, executable);
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

    private void mockEmailProcessing() {
        String expectedEmailContent = "Expected Email Content";
        when(templateEngine.process(eq("email"), any(Context.class))).thenReturn(expectedEmailContent);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }
}