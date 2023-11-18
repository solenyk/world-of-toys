package com.kopchak.worldoftoys.repository.order;

import com.kopchak.worldoftoys.model.order.Order;
import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.model.user.AppUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = "products")
    Set<Order> findAllByUser(AppUser user);


//    @Query("SELECT p " +
//            "FROM Order o " +
//            "JOIN o.products p " +
//            "WHERE o.user = :user")
//    Set<Set<Product>> findAllOrderProductsByUser(AppUser user);
}
