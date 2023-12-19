package com.kopchak.worldoftoys.repository.order;

import com.kopchak.worldoftoys.model.order.Order;
import com.kopchak.worldoftoys.model.order.OrderStatus;
import com.kopchak.worldoftoys.model.order.payment.PaymentStatus;
import com.kopchak.worldoftoys.model.user.AppUser;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = "orderDetails.product")
    Set<Order> findAllByUser(AppUser user);

    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"orderDetails.product", "payments"})
    @NotNull Optional<Order> findById(@NotNull String id);

    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.payments p " +
            "WHERE (:orderStatuses IS NULL OR o.orderStatus IN :orderStatuses) " +
            "AND (:paymentStatuses IS NULL OR p.status IN :paymentStatuses) " +
            "ORDER BY " +
            "CASE WHEN :sortDirection = 'DESC' THEN o.dateTime END DESC, " +
            "CASE WHEN :sortDirection = 'ASC' THEN o.dateTime END ASC")
    Page<Order> filterOrdersByStatusesAndDate(@Param("orderStatuses") List<OrderStatus> orderStatuses,
                                              @Param("paymentStatuses") List<PaymentStatus> paymentStatuses,
                                              @Param("sortDirection") String sortDirection,
                                              Pageable pageable);

    @Query("SELECT DISTINCT o.orderStatus FROM Order o")
    Set<OrderStatus> findAllOrderStatuses();

    @Query("SELECT DISTINCT p.status FROM Order o LEFT JOIN o.payments p")
    Set<PaymentStatus> findAllPaymentStatuses();
}
