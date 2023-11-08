package com.kopchak.worldoftoys.dto.cart;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CartItemDto(String name, String slug, BigDecimal totalProductPrice, Integer quantity) {
}
