package com.kopchak.worldoftoys.dto.cart;

import lombok.Builder;

import java.math.BigDecimal;
import java.math.BigInteger;

@Builder
public record CartItemDto(String name, String slug, BigDecimal totalProductPrice, BigInteger quantity) {
}
