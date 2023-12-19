package com.kopchak.worldoftoys.dto.admin.product.order;

import com.kopchak.worldoftoys.dto.order.OrderDto;

import java.util.Set;

public record FilteredOrdersPageDto(Set<AdminOrderDto> content, long totalElementsAmount, long totalPagesAmount) {
}
