package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.payment.StripeCredentialsDto;
import com.stripe.exception.StripeException;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

public interface PaymentService {
    String stripeCheckout(StripeCredentialsDto credentialsDto, String orderId);
    boolean isNonExistentOrPaidOrder(String orderId);
    void handlePaymentWebhook(HttpServletRequest request) throws StripeException, IOException;
}
