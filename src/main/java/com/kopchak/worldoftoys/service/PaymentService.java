package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.payment.StripeCredentialsDto;
import com.kopchak.worldoftoys.exception.exception.order.InvalidOrderException;
import com.kopchak.worldoftoys.exception.exception.email.MessageSendingException;
import com.stripe.exception.StripeException;

public interface PaymentService {
    String stripeCheckout(StripeCredentialsDto credentialsDto, String orderId)
            throws StripeException, InvalidOrderException;

    void handlePaymentWebhook(String sigHeader, String requestBody) throws StripeException, MessageSendingException;
}
