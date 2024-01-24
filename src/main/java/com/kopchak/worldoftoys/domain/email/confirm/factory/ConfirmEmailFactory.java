package com.kopchak.worldoftoys.domain.email.confirm.factory;

import com.kopchak.worldoftoys.domain.email.confirm.ConfirmEmail;
import com.kopchak.worldoftoys.domain.email.confirm.ext.AccountActivationEmail;
import com.kopchak.worldoftoys.domain.email.confirm.ext.ResetPasswordEmail;
import com.kopchak.worldoftoys.domain.token.confirm.ConfirmationTokenType;

public class ConfirmEmailFactory {
    public ConfirmEmail createConfirmEmail(ConfirmationTokenType tokenType, String confirmToken) {
        if (tokenType == ConfirmationTokenType.ACTIVATION) {
            return new AccountActivationEmail(confirmToken);
        }
        return new ResetPasswordEmail(confirmToken);
    }
}
