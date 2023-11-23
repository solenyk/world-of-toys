package com.kopchak.worldoftoys.dto.order;

import java.math.BigDecimal;

public record OrderProductDto(String name, BigDecimal price, Integer quantity) {
}
