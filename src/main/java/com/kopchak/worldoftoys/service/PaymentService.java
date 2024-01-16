package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.payment.StripeCredentialsDto;
import com.kopchak.worldoftoys.exception.InvalidOrderException;
import com.kopchak.worldoftoys.exception.MessageSendingException;
import com.stripe.exception.StripeException;

public interface PaymentService {
    String stripeCheckout(StripeCredentialsDto credentialsDto, String orderId) throws StripeException, InvalidOrderException;
    void handlePaymentWebhook(String sigHeader, String requestBody) throws StripeException, MessageSendingException;
}
