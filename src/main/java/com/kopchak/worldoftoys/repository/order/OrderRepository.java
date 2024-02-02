package com.kopchak.worldoftoys.repository.order;

import com.kopchak.worldoftoys.domain.order.Order;
import com.kopchak.worldoftoys.domain.order.OrderStatus;
import com.kopchak.worldoftoys.domain.order.payment.PaymentStatus;
import com.kopchak.worldoftoys.domain.user.AppUser;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface OrderRepository extends JpaRepository<Order, String>, JpaSpecificationExecutor<Order> {
    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = "orderDetails.product")
    Set<Order> findAllByUser(AppUser user);

    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"orderDetails.product", "payments",
            "user"})
    @NotNull Optional<Order> findById(@NotNull String id);

    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"orderDetails.product", "payments"})
    @NotNull Page<Order> findAll(Specification<Order> spec, @NotNull Pageable pageable);

    @Query("SELECT DISTINCT o.orderStatus FROM Order o WHERE o.orderStatus IS NOT NULL")
    Set<OrderStatus> findAllOrderStatuses();

    @Query("SELECT DISTINCT p.status FROM Order o LEFT JOIN o.payments p WHERE p IS NOT NULL")
    Set<PaymentStatus> findAllPaymentStatuses();
}
