package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.payment.StripeCredentialsDto;
import com.kopchak.worldoftoys.exception.exception.MessageSendingException;
import com.stripe.exception.StripeException;

public interface PaymentService {
    String stripeCheckout(StripeCredentialsDto credentialsDto, String orderId) throws StripeException;
    boolean isNonExistentOrPaidOrder(String orderId);
    void handlePaymentWebhook(String sigHeader, String requestBody) throws StripeException, MessageSendingException;
}
