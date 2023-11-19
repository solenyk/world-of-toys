package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.payment.StripeCredentialsDto;

public interface PaymentService {
    String stripeCheckout(StripeCredentialsDto credentialsDto, String orderId);
    boolean isNonExistentOrPaidOrder(String orderId);
    void handlePaymentWebhook(String sigHeader, String requestBody);
}
