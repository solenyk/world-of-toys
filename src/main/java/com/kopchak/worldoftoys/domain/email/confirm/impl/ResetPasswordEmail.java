package com.kopchak.worldoftoys.domain.email.confirm.impl;

import com.kopchak.worldoftoys.domain.email.confirm.ConfirmEmailType;

public class ResetPasswordEmail extends ConfirmEmailType {
    private static final String TITLE = "Password reset";
    private static final String SUBJECT = "Reset your password";
    private static final String LINK = "/api/v1/auth/reset-password";
    private static final String LINK_NAME = "Reset password";
    private static final String MESSAGE = "Thank you for using our website. " +
            "Please click on the below link to reset your password. Link will expire in 15 minutes.";

    public ResetPasswordEmail(String confirmToken) {
        super(TITLE, SUBJECT, buildActivationLink(confirmToken, LINK), LINK_NAME, MESSAGE);
    }
}
