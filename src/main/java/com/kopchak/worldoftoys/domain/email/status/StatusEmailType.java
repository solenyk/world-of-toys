package com.kopchak.worldoftoys.domain.email.status;

import com.kopchak.worldoftoys.domain.email.EmailType;
import com.kopchak.worldoftoys.domain.order.StatusProvider;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

public abstract class StatusEmailType extends EmailType {
    private final static String LOGIN_LINK = "/api/v1/auth/login";

    public StatusEmailType(String title, String subject, String link, String linkName, String msg) {
        super(title, subject, link, linkName, msg);
    }

    protected static String buildActivationLink() {
        UriComponentsBuilder uriBuilder = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(LOGIN_LINK);
        return uriBuilder.toUriString();
    }

    protected static <T extends Enum<T> & StatusProvider> String buildMessage(String msg, String orderId, T status) {
        return String.format(msg, orderId, status.getStatus());
    }
}
