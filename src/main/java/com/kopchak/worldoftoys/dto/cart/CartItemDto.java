package com.kopchak.worldoftoys.dto.cart;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
//public record CartItemProductDto(String name, String slug, Image mainImage, BigDecimal totalProductPrice, Integer quantity) {
public record CartItemDto(String name, String slug, BigDecimal totalProductPrice, Integer quantity) {
}
