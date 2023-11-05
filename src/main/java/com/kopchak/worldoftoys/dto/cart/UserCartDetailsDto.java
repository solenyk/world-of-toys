package com.kopchak.worldoftoys.dto.cart;

import java.math.BigDecimal;
import java.util.Set;

public record UserCartDetailsDto(Set<CartItemDto> content, BigDecimal totalCost){
}
