package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.exception.UserNotFoundException;
import com.kopchak.worldoftoys.model.order.StatusProvider;
import com.kopchak.worldoftoys.model.order.payment.PaymentStatus;
import com.kopchak.worldoftoys.model.token.ConfirmationTokenType;
import com.kopchak.worldoftoys.model.user.AppUser;
import com.kopchak.worldoftoys.repository.user.UserRepository;
import com.kopchak.worldoftoys.service.EmailSenderService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

@Service
@AllArgsConstructor
@Slf4j
public class EmailSenderServiceImpl implements EmailSenderService {
    private final static String SENDER_EMAIL = "worldoftoys@gmail.com";
    private final static String ACCOUNT_ACTIVATION_TITLE = "Account activation";
    private final static String PASSWORD_RESET_TITLE = "Password reset";
    private final static String PAYMENT_STATUS_TITLE = "Order payment";
    private final static String ORDER_STATUS_TITLE = "Order status";
    private final static String ACCOUNT_ACTIVATION_SUBJECT = "Confirm your email";
    private final static String PASSWORD_RESET_SUBJECT = "Reset your password";
    private final static String PAYMENT_STATUS_SUBJECT = "Order payment status";
    private final static String ORDER_STATUS_SUBJECT = "Order status";
    private final static String ACCOUNT_ACTIVATION_LINK = "/api/v1/auth/confirm?token=";
    private final static String PASSWORD_RESET_LINK = "/api/v1/auth/reset-password?token=";
    private final static String LOGIN_LINK = "/api/v1/auth/login";
    private final static String ACCOUNT_ACTIVATION_LINK_NAME = "Activate Now";
    private final static String PASSWORD_RESET_LINK_NAME = "Reset password";
    private final static String PAYMENT_STATUS_LINK_NAME = "Check order payment status";
    private final static String ORDER_STATUS_LINK_NAME = "Check current order status";
    private final static String ACCOUNT_ACTIVATION_MSG =
            "Thank you for registering. Please click on the below link to activate your account. " +
                    "Link will expire in 15 minutes.";
    private final static String PASSWORD_RESET_MSG = "Thank you for using our website. " +
            "Please click on the below link to reset your password. Link will expire in 15 minutes.";
    private final static String PAYMENT_STATUS_MSG = "Order №%s payment status is %s. For more information, " +
            "visit our website:";
    private final static String ORDER_STATUS_MSG = "Order №%s status is %s. For more information, " +
            "visit our website:";
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private ITemplateEngine templateEngine;
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

    @Override
    public <T extends Enum<T> & StatusProvider> void sendEmail(String userEmail, String userFirstname,
                                                               String orderId, T status) {
        if(status instanceof PaymentStatus){
            String msg = String.format(PAYMENT_STATUS_MSG, orderId, status.getStatus());
            String emailContent = buildEmail(PAYMENT_STATUS_TITLE, userFirstname, msg, getBaseUrl() + LOGIN_LINK,
                    PAYMENT_STATUS_LINK_NAME);
            send(userEmail, emailContent, PAYMENT_STATUS_SUBJECT);
        } else {
            String msg = String.format(ORDER_STATUS_MSG, orderId, status.getStatus());
            String emailContent = buildEmail(ORDER_STATUS_TITLE, userFirstname, msg, getBaseUrl() + LOGIN_LINK,
                    ORDER_STATUS_LINK_NAME);
            send(userEmail, emailContent, ORDER_STATUS_SUBJECT);
        }
    }

    private String buildEmail(String title, String name, String message, String link, String linkName) {
        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("name", name);
        context.setVariable("message", message);
        context.setVariable("link", link);
        context.setVariable("linkName", linkName);
        return templateEngine.process("email", context);
    }

    private @NotNull String getBaseUrl() {
        String requestUrl = request.getRequestURL().toString();
        String servletPath = request.getServletPath();
        return requestUrl.substring(0, requestUrl.length() - servletPath.length());
    }
}
