package com.kopchak.worldoftoys.dto.admin.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kopchak.worldoftoys.dto.order.OrderProductDto;
import com.kopchak.worldoftoys.domain.order.OrderStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Builder
public record AdminOrderDto(String id, @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime dateTime,
                            OrderStatus orderStatus, Set<PaymentDto> payments, Set<OrderProductDto> products,
                            BigDecimal totalPrice) {
}
