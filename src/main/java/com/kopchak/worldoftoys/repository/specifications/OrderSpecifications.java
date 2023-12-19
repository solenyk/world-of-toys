package com.kopchak.worldoftoys.repository.specifications;

import com.kopchak.worldoftoys.model.order.Order;
import com.kopchak.worldoftoys.model.order.OrderStatus;
import com.kopchak.worldoftoys.model.order.payment.PaymentStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface OrderSpecifications {
    Specification<Order> filterByStatusesAndDate(List<OrderStatus> orderStatuses, List<PaymentStatus> paymentStatuses,
                                                 String sortDirection);
}
