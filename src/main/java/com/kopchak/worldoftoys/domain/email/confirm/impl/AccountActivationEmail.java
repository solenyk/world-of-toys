package com.kopchak.worldoftoys.domain.email.confirm.impl;

import com.kopchak.worldoftoys.domain.email.confirm.ConfirmEmailType;

public class AccountActivationEmail extends ConfirmEmailType {
    private static final String TITLE = "Account activation";
    private static final String SUBJECT = "Confirm your email";
    private static final String LINK = "/api/v1/auth/confirm";
    private static final String LINK_NAME = "Activate Now";
    private static final String MESSAGE =
            "Thank you for registering. Please click on the below link to activate your account. " +
                    "Link will expire in 15 minutes.";

    public AccountActivationEmail(String confirmToken) {
        super(TITLE, SUBJECT, buildActivationLink(confirmToken, LINK), LINK_NAME, MESSAGE);
    }
}
