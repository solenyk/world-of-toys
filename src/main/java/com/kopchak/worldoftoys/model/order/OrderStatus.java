package com.kopchak.worldoftoys.model.order;

public enum OrderStatus {
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
}
