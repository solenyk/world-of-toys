package com.kopchak.worldoftoys.domain.email.status.factory;

import com.kopchak.worldoftoys.domain.email.status.StatusEmail;
import com.kopchak.worldoftoys.domain.email.status.ext.OrderStatusEmail;
import com.kopchak.worldoftoys.domain.email.status.ext.PaymentStatusEmail;
import com.kopchak.worldoftoys.domain.order.StatusProvider;
import com.kopchak.worldoftoys.domain.order.payment.PaymentStatus;

public class StatusEmailFactory {
    public <T extends Enum<T> & StatusProvider> StatusEmail createStatusEmail(T status, String orderId) {
        if (status instanceof PaymentStatus) {
            return new PaymentStatusEmail(orderId, status);
        }
        return new OrderStatusEmail(orderId, status);
    }
}
