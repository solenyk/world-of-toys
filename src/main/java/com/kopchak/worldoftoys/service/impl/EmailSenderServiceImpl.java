package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.exception.UserNotFoundException;
import com.kopchak.worldoftoys.model.token.ConfirmationTokenType;
import com.kopchak.worldoftoys.model.user.AppUser;
import com.kopchak.worldoftoys.repository.user.UserRepository;
import com.kopchak.worldoftoys.service.EmailSenderService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@AllArgsConstructor
@Slf4j
public class EmailSenderServiceImpl implements EmailSenderService {
    private final static String SENDER_EMAIL = "worldoftoys@gmail.com";
    private final static String ACCOUNT_ACTIVATION_TITLE = "Account activation";
    private final static String PASSWORD_RESET_TITLE = "Password reset";
    private final static String ACCOUNT_ACTIVATION_SUBJECT = "Confirm your email";
    private final static String PASSWORD_RESET_SUBJECT = "Reset your password";
    private final static String ACCOUNT_ACTIVATION_LINK = "/api/v1/auth/confirm?token=";
    private final static String PASSWORD_RESET_LINK = "/api/v1/auth/reset-password?token=";
    private final static String ACCOUNT_ACTIVATION_LINK_NAME = "Activate Now";
    private final static String PASSWORD_RESET_LINK_NAME = "Reset password";
    private final static String ACCOUNT_ACTIVATION_MSG =
            "Thank you for registering. Please click on the below link to activate your account:";
    private final static String PASSWORD_RESET_MSG =
            "Thank you for using our website. Please click on the below link to reset your password:";
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private TemplateEngine templateEngine;
    private final HttpServletRequest request;

    @Override
    @Async
    public void send(String emailRecipient, String emailContent, String msgSubject) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setText(emailContent, true);
            helper.setTo(emailRecipient);
            helper.setSubject(msgSubject);
            helper.setFrom(SENDER_EMAIL);
            mailSender.send(mimeMessage);
            log.info("Email sent to: {}", emailRecipient);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {} with subject: {}", emailRecipient, msgSubject, e);
            throw new IllegalStateException("Failed to send email");
        }
    }

    @Override
    public void sendEmail(String userEmail, String confirmToken, ConfirmationTokenType tokenType) {
        String emailContent;
        AppUser user = userRepository.findByEmail(userEmail).orElseThrow(() ->
                new UserNotFoundException(HttpStatus.NOT_FOUND, "User with this username does not exist!"));
        String fullName = user.getFirstname() + " " + user.getLastname();
        if (tokenType == ConfirmationTokenType.ACTIVATION) {
            emailContent = buildEmail(ACCOUNT_ACTIVATION_TITLE, fullName, ACCOUNT_ACTIVATION_MSG,
                    getBaseUrl() + ACCOUNT_ACTIVATION_LINK + confirmToken, ACCOUNT_ACTIVATION_LINK_NAME);
            send(userEmail, emailContent, ACCOUNT_ACTIVATION_SUBJECT);
        } else {
            emailContent = buildEmail(PASSWORD_RESET_TITLE, fullName, PASSWORD_RESET_MSG,
                    getBaseUrl() + PASSWORD_RESET_LINK + confirmToken, PASSWORD_RESET_LINK_NAME);
            send(userEmail, emailContent, PASSWORD_RESET_SUBJECT);
        }
    }

    private String buildEmail(String title, String name, String message, String link, String linkName) {
        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("name", name);
        context.setVariable("message", message);
        context.setVariable("link", link);
        context.setVariable("linkName", linkName);
        return templateEngine.process("email-template", context);
    }

    private String getBaseUrl() {
        String requestUrl = request.getRequestURL().toString();
        String servletPath = request.getServletPath();
        return requestUrl.substring(0, requestUrl.length() - servletPath.length());
    }
}
