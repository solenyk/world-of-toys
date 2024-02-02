package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.domain.email.EmailType;
import com.kopchak.worldoftoys.domain.email.confirm.ConfirmEmail;
import com.kopchak.worldoftoys.domain.email.confirm.factory.ConfirmEmailFactory;
import com.kopchak.worldoftoys.domain.email.status.StatusEmail;
import com.kopchak.worldoftoys.domain.email.status.factory.StatusEmailFactory;
import com.kopchak.worldoftoys.domain.order.StatusProvider;
import com.kopchak.worldoftoys.domain.token.confirm.ConfirmationTokenType;
import com.kopchak.worldoftoys.domain.user.AppUser;
import com.kopchak.worldoftoys.exception.exception.email.MessageSendingException;
import com.kopchak.worldoftoys.exception.exception.user.UserNotFoundException;
import com.kopchak.worldoftoys.repository.user.UserRepository;
import com.kopchak.worldoftoys.service.EmailSenderService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
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
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private ITemplateEngine templateEngine;

    @Override
    @Async
    public void send(String emailRecipient, String emailContent, String msgSubject) throws MessageSendingException {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setText(emailContent, true);
            helper.setTo(emailRecipient);
            helper.setSubject(msgSubject);
            helper.setFrom(SENDER_EMAIL);
            mailSender.send(mimeMessage);
            log.info("The email with subject: {} sent to the: {}", msgSubject, emailRecipient);
        } catch (MessagingException | MailException e) {
            log.error("Failed to send the email to the user with username: {}", emailRecipient);
            throw new MessageSendingException(String.format("Failed to send the email: %s", e.getMessage()));
        }
    }

    @Override
    public void sendEmail(String userEmail, String confirmToken, ConfirmationTokenType tokenType)
            throws UserNotFoundException, MessageSendingException {
        AppUser user = userRepository.findByEmail(userEmail).orElseThrow(() -> {
            String errMsg = String.format("The user with username: %s does not exist!", userEmail);
            log.error(errMsg);
            return new UserNotFoundException(errMsg);
        });
        String userFirstname = user.getFirstname();
        ConfirmEmailFactory confirmEmailFactory = new ConfirmEmailFactory();
        ConfirmEmail confirmEmail = confirmEmailFactory.createConfirmEmail(tokenType, confirmToken);
        String emailContent = buildEmail(userFirstname, confirmEmail);
        send(userEmail, emailContent, confirmEmail.getSubject());
    }

    @Override
    public <T extends Enum<T> & StatusProvider> void sendEmail(String userEmail, String userFirstname, String orderId,
                                                               T status) throws MessageSendingException {
        StatusEmailFactory statusEmailFactory = new StatusEmailFactory();
        StatusEmail statusEmail = statusEmailFactory.createStatusEmail(status, orderId);
        String emailContent = buildEmail(userFirstname, statusEmail);
        send(userEmail, emailContent, statusEmail.getSubject());
    }

    private String buildEmail(String name, EmailType emailType) {
        Context context = new Context();
        context.setVariable("title", emailType.getTitle());
        context.setVariable("name", name);
        context.setVariable("message", emailType.getMsg());
        context.setVariable("link", emailType.getLink());
        context.setVariable("linkName", emailType.getLinkName());
        return templateEngine.process("email", context);
    }
}
