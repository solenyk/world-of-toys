package com.kopchak.worldoftoys.repository.specifications.impl;

import com.kopchak.worldoftoys.model.order.Order;
import com.kopchak.worldoftoys.model.order.OrderStatus;
import com.kopchak.worldoftoys.model.order.Order_;
import com.kopchak.worldoftoys.model.order.payment.Payment;
import com.kopchak.worldoftoys.model.order.payment.PaymentStatus;
import com.kopchak.worldoftoys.model.order.payment.Payment_;
import com.kopchak.worldoftoys.repository.specifications.OrderSpecifications;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.SetJoin;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderSpecificationsImpl implements OrderSpecifications {
    @Override
    public Specification<Order> filterByStatusesAndDate(List<OrderStatus> orderStatuses,
                                                        List<PaymentStatus> paymentStatuses, String sortDirection) {
        return Specification
                .where(hasOrderInStatus(orderStatuses))
                .and(hasOrderInPaymentStatus(paymentStatuses))
                .and(sortByDate(sortDirection));
    }

    private Specification<Order> hasOrderInStatus(List<OrderStatus> orderStatuses) {
        return (root, query, criteriaBuilder) -> {
            if (orderStatuses == null || orderStatuses.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return root.get(Order_.ORDER_STATUS).in(orderStatuses);
        };
    }

    private Specification<Order> hasOrderInPaymentStatus(List<PaymentStatus> paymentStatuses) {
        return (root, query, criteriaBuilder) -> {
            if (paymentStatuses == null || paymentStatuses.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            SetJoin<Order, Payment> orderPaymentSetJoin = root.joinSet(Order_.PAYMENTS, JoinType.INNER);
            Expression<PaymentStatus> paymentStatusExpression = orderPaymentSetJoin.get(Payment_.STATUS);

            Predicate[] predicates = paymentStatuses.stream()
                    .map(status -> criteriaBuilder.equal(paymentStatusExpression, status))
                    .toArray(Predicate[]::new);

            return criteriaBuilder.or(predicates);
        };
    }

    private Specification<Order> sortByDate(String sortOrder) {
        return (root, query, criteriaBuilder) -> {
            if ("asc".equalsIgnoreCase(sortOrder)) {
                query.orderBy(criteriaBuilder.asc(root.get(Order_.DATE_TIME)));
            } else if ("desc".equalsIgnoreCase(sortOrder)) {
                query.orderBy(criteriaBuilder.desc(root.get(Order_.DATE_TIME)));
            }
            return query.getRestriction();
        };
    }
}
