package com.kopchak.worldoftoys.repository.cart;

import com.kopchak.worldoftoys.dto.cart.CartItemDto;
import com.kopchak.worldoftoys.model.cart.CartItem;
import com.kopchak.worldoftoys.model.cart.CartItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Set;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, CartItemId> {
    @Query("SELECT new com.kopchak.worldoftoys.dto.cart.CartItemDto(p.name, p.slug, p.price * c.quantity, c.quantity) " +
            "FROM CartItem c " +
            "JOIN c.id.product p " +
            "WHERE c.id.user.email = :email")
    Set<CartItemDto> findAllCartItemDtosByUserEmail(@Param("email") String email);

    @Query("SELECT SUM(p.price * c.quantity) " +
            "FROM CartItem c " +
            "JOIN c.id.product p " +
            "WHERE c.id.user.email = :email")
    BigDecimal calculateUserCartTotalByEmail(@Param("email") String email);
}
