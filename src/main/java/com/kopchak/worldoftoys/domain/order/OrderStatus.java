package com.kopchak.worldoftoys.domain.order;

public enum OrderStatus implements StatusProvider {
    AWAITING_PAYMENT("Awaiting payment"),
    AWAITING_FULFILMENT("Awaiting  fulfilment"),
    AWAITING_SHIPPING("Awaiting  shipping"),
    SHIPPED("Shipped"),
    COMPLETED("Completed"),
    CANCELED("Canceled");

    public final String status;

    OrderStatus(String status) {
        this.status = status;
    }

    @Override
    public String getStatus() {
        return this.status;
    }
}
