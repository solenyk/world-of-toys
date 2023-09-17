package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.model.token.ConfirmTokenType;
import com.kopchak.worldoftoys.service.EmailSenderService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@AllArgsConstructor
public class EmailSenderServiceImpl {
    private final static Logger LOGGER = LoggerFactory.getLogger(EmailSenderServiceImpl.class);
    private final static String SENDER_EMAIL = "worldoftoys@gmail.com";
    private final static String ACCOUNT_ACTIVATION_TITLE = "Account activation";
    private final static String PASSWORD_RESET_TITLE = "Password reset";
    private final static String ACCOUNT_ACTIVATION_SUBJECT = "Confirm your email";
    private final static String PASSWORD_RESET_SUBJECT = "Reset your password";
    private final static String ACCOUNT_ACTIVATION_LINK = "/api/v1/auth/confirm?token=";
    private final static String PASSWORD_RESET_LINK = "/api/v1/auth/forgot-password?token=";
    private final static String ACCOUNT_ACTIVATION_LINK_NAME = "Activate Now";
    private final static String PASSWORD_RESET_LINK_NAME = "Reset password";
    private final static String ACCOUNT_ACTIVATION_MSG =
            "Thank you for registering. Please click on the below link to activate your account:";
    private final static String PASSWORD_RESET_MSG =
            "Thank you for using our website. Please click on the below link to reset your password:";
    private final JavaMailSender mailSender;
    private TemplateEngine templateEngine;
    private final HttpServletRequest request;

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
