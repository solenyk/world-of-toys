package com.kopchak.worldoftoys.model.order.payment;

import com.kopchak.worldoftoys.model.order.StatusProvider;

public enum PaymentStatus implements StatusProvider {
    PENDING("Pending"),
    COMPLETE("Complete"),
    FAILED("Failed");

    public final String status;

    PaymentStatus(String status) {
        this.status = status;
    }

    @Override
    public String getStatus() {
        return this.status;
    }
}
