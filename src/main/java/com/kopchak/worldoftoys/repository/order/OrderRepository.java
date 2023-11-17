package com.kopchak.worldoftoys.repository.order;

import com.kopchak.worldoftoys.model.order.OrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<OrderDetails, Integer> {
}
