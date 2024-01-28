package com.kopchak.worldoftoys.dto.cart;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.Set;

@Builder
public record UserCartDetailsDto(Set<CartItemDto> content, BigDecimal totalCost) {
}
