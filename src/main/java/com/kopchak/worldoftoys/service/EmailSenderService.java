package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.model.token.ConfirmTokenType;

public interface EmailSenderService {
    void send(String recipientEmail, String senderEmail, String msgSubject);
    void sendEmail(String userEmail, String userFirstname, String confirmToken, ConfirmTokenType tokenType);
}
