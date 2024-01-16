package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.domain.email.EmailType;
import com.kopchak.worldoftoys.domain.email.confirm.ConfirmEmailType;
import com.kopchak.worldoftoys.domain.email.confirm.impl.AccountActivationEmail;
import com.kopchak.worldoftoys.domain.email.confirm.impl.ResetPasswordEmail;
import com.kopchak.worldoftoys.domain.email.status.StatusEmailType;
import com.kopchak.worldoftoys.domain.email.status.impl.OrderStatusEmail;
import com.kopchak.worldoftoys.domain.email.status.impl.PaymentStatusEmail;
import com.kopchak.worldoftoys.domain.order.StatusProvider;
import com.kopchak.worldoftoys.domain.order.payment.PaymentStatus;
import com.kopchak.worldoftoys.domain.token.ConfirmationTokenType;
import com.kopchak.worldoftoys.domain.user.AppUser;
import com.kopchak.worldoftoys.exception.MessageSendingException;
import com.kopchak.worldoftoys.exception.UserNotFoundException;
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
        ConfirmEmailType confirmEmailType;
        if (tokenType == ConfirmationTokenType.ACTIVATION) {
            confirmEmailType = new AccountActivationEmail(confirmToken);
        } else {
            confirmEmailType = new ResetPasswordEmail(confirmToken);
        }
        String emailContent = buildEmail(userFirstname, confirmEmailType);
        send(userEmail, emailContent, confirmEmailType.getSubject());
    }

    @Override
    public <T extends Enum<T> & StatusProvider> void sendEmail(String userEmail, String userFirstname, String orderId,
                                                               T status) throws MessageSendingException {
        StatusEmailType statusEmailType;
        if (status instanceof PaymentStatus) {
            statusEmailType = new PaymentStatusEmail(orderId, status);
        } else {
            statusEmailType = new OrderStatusEmail(orderId, status);
        }
        String emailContent = buildEmail(userFirstname, statusEmailType);
        send(userEmail, emailContent, statusEmailType.getSubject());
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
