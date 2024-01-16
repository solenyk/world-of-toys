package com.kopchak.worldoftoys.dto.admin.product.order;

import java.util.Set;

public record FilteringOrderOptionsDto(Set<StatusDto> orderStatuses, Set<StatusDto> paymentStatuses) {
}
