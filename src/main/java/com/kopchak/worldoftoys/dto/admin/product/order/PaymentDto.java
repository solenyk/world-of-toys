package com.kopchak.worldoftoys.dto.admin.product.order;

import com.kopchak.worldoftoys.model.order.payment.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentDto(PaymentStatus status, LocalDateTime dateTime) {
}
