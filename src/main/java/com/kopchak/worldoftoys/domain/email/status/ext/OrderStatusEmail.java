package com.kopchak.worldoftoys.domain.email.status.ext;

import com.kopchak.worldoftoys.domain.email.status.StatusEmail;
import com.kopchak.worldoftoys.domain.order.StatusProvider;

public class OrderStatusEmail extends StatusEmail {
    private static final String TITLE = "Order status";
    private static final String SUBJECT = "Order status";
    private static final String LINK_NAME = "Check current order status";
    private static final String MESSAGE = "Order №%s status is %s. For more information, " +
            "visit our website:";

    public <T extends Enum<T> & StatusProvider> OrderStatusEmail(String orderId, T status) {
        super(TITLE, SUBJECT, buildActivationLink(), LINK_NAME, buildMessage(MESSAGE, orderId, status));
    }
}
