package com.kopchak.worldoftoys.dto.admin.product.order;

import java.util.Set;

public record FilteredOrdersPageDto(Set<AdminOrderDto> content, long totalElementsAmount, long totalPagesAmount) {
}
