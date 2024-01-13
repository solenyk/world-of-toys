package com.kopchak.worldoftoys.domain.email.status.impl;

import com.kopchak.worldoftoys.domain.email.status.StatusEmailType;
import com.kopchak.worldoftoys.domain.order.StatusProvider;

public class PaymentStatusEmail extends StatusEmailType {
    private static final String TITLE = "Order payment";
    private static final String SUBJECT = "Order payment status";
    private static final String LINK_NAME = "Check order payment status";
    private static final String MESSAGE = "Order №%s payment status is %s. For more information, " +
            "visit our website:";

    public <T extends Enum<T> & StatusProvider> PaymentStatusEmail(String orderId, T status) {
        super(TITLE, SUBJECT, buildActivationLink(), LINK_NAME, buildMessage(MESSAGE, orderId, status));
    }
}
