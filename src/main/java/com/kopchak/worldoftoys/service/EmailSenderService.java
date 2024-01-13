package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.exception.MessageSendingException;
import com.kopchak.worldoftoys.exception.UserNotFoundException;
import com.kopchak.worldoftoys.model.order.StatusProvider;
import com.kopchak.worldoftoys.model.token.ConfirmationTokenType;

public interface EmailSenderService {
    void send(String recipientEmail, String senderEmail, String msgSubject) throws MessageSendingException;
    void sendEmail(String userEmail, String confirmToken, ConfirmationTokenType tokenType) throws UserNotFoundException, MessageSendingException;
    <T extends Enum<T> & StatusProvider> void sendEmail(String userEmail, String userFirstname, String orderId, T status) throws MessageSendingException;
}
