package com.kopchak.worldoftoys.dto.admin.order;

import com.kopchak.worldoftoys.domain.order.payment.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentDto(PaymentStatus status, LocalDateTime dateTime) {
}
