package com.kopchak.worldoftoys.dto.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kopchak.worldoftoys.domain.order.OrderStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Builder
public record OrderDto(String id, @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime dateTime,
                       OrderStatus orderStatus, Set<OrderProductDto> products, BigDecimal totalPrice) {
}
