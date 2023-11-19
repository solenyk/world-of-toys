package com.kopchak.worldoftoys.repository.order;

import com.kopchak.worldoftoys.model.order.Order;
import com.kopchak.worldoftoys.model.user.AppUser;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = "orderDetails.product")
    Set<Order> findAllByUser(AppUser user);

    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"orderDetails.product", "payments"})
    @NotNull Optional<Order> findById(@NotNull String id);
}
