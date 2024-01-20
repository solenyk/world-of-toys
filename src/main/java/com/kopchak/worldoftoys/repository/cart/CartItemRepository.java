package com.kopchak.worldoftoys.repository.cart;

import com.kopchak.worldoftoys.domain.cart.CartItem;
import com.kopchak.worldoftoys.domain.cart.CartItemId;
import com.kopchak.worldoftoys.domain.product.Product;
import com.kopchak.worldoftoys.domain.user.AppUser;
import com.kopchak.worldoftoys.dto.cart.CartItemDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, CartItemId> {
    @Query("SELECT new com.kopchak.worldoftoys.dto.cart.CartItemDto(p.name, p.slug, p.price * c.quantity, c.quantity) " +
            "FROM CartItem c " +
            "JOIN c.id.product p " +
            "WHERE c.id.user = :user")
    Set<CartItemDto> findAllUserCartItems(@Param("user") AppUser user);

    @Query("SELECT SUM(p.price * c.quantity) " +
            "FROM CartItem c " +
            "JOIN c.id.product p " +
            "WHERE c.id.user = :user")
    BigDecimal calculateUserCartTotalPrice(@Param("user") AppUser user);

    @Modifying
    @Transactional
    Set<CartItem> deleteAllById_User(AppUser user);

    @Modifying
    @Transactional
    void deleteAllById_Product(Product product);

    @Modifying
    @Transactional
    @Query("UPDATE CartItem c " +
            "SET c.quantity = (SELECT p.availableQuantity FROM Product p WHERE c.id.product = p) " +
            "WHERE c.quantity > (SELECT p.availableQuantity FROM Product p WHERE c.id.product = p) " +
            "AND c.id.user = :user")
    int updateCartItems(@Param("user") AppUser user);

    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem c " +
            "WHERE c.id.user = :user AND " +
            "c.id.product IN " +
            " (SELECT p FROM Product p " +
            " WHERE p.availableQuantity = 0 OR NOT p.isAvailable)")
    int deleteUnavailableItems(@Param("user") AppUser user);

}
