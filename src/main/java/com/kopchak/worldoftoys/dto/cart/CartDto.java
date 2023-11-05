package com.kopchak.worldoftoys.dto.cart;

import java.math.BigDecimal;
import java.util.Set;

public record CartDto (Set<CartItemProductDto> content, BigDecimal totalCost){
}
