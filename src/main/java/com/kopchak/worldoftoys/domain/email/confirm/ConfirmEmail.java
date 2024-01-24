package com.kopchak.worldoftoys.domain.email.confirm;

import com.kopchak.worldoftoys.domain.email.EmailType;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

public abstract class ConfirmEmail extends EmailType {
    private final static String TOKEN_PARAM_NAME = "token";

    public ConfirmEmail(String title, String subject, String link, String linkName, String msg) {
        super(title, subject, link, linkName, msg);
    }

    protected static String buildActivationLink(String confirmToken, String link) {
        UriComponentsBuilder uriBuilder = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(link)
                .queryParam(TOKEN_PARAM_NAME, confirmToken);
        return uriBuilder.toUriString();
    }
}
