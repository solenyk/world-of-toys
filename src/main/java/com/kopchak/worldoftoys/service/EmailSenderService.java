package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.model.order.StatusProvider;
import com.kopchak.worldoftoys.model.token.ConfirmationTokenType;

public interface EmailSenderService {
    void send(String recipientEmail, String senderEmail, String msgSubject);
    void sendEmail(String userEmail, String confirmToken, ConfirmationTokenType tokenType);
    <T extends Enum<T> & StatusProvider> void sendEmail(String userEmail, String userFirstname, String orderId, T status);
}
