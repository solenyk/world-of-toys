package com.kopchak.worldoftoys.dto.admin.order;

import java.util.Set;

public record FilteringOrderOptionsDto(Set<StatusDto> orderStatuses, Set<StatusDto> paymentStatuses) {
}
