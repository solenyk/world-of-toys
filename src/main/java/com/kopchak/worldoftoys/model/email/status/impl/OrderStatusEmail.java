package com.kopchak.worldoftoys.model.email.status.impl;

import com.kopchak.worldoftoys.model.email.status.StatusEmailType;
import com.kopchak.worldoftoys.model.order.StatusProvider;

public class OrderStatusEmail extends StatusEmailType {
    private static final String TITLE = "Order status";
    private static final String SUBJECT = "Order status";
    private static final String LINK_NAME = "Check current order status";
    private static final String MESSAGE = "Order №%s status is %s. For more information, " +
            "visit our website:";

    public <T extends Enum<T> & StatusProvider> OrderStatusEmail(String orderId, T status) {
        super(TITLE, SUBJECT, buildActivationLink(), LINK_NAME, buildMessage(MESSAGE, orderId, status));
    }
}
