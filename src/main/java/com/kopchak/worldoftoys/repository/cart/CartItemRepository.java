package com.kopchak.worldoftoys.repository.cart;

import com.kopchak.worldoftoys.dto.cart.CartItemDto;
import com.kopchak.worldoftoys.model.cart.CartItem;
import com.kopchak.worldoftoys.model.cart.CartItemId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO cart_item (user_id, product_id, quantity) " +
            "VALUES(" +
            "(SELECT u.id FROM app_user u WHERE u.email = :userEmail)," +
            "(SELECT p.id FROM product p WHERE p.slug = :productSlug)," +
            ":quantity" +
            ") ON DUPLICATE KEY UPDATE quantity = quantity + :quantity", nativeQuery = true)
    void insertUserCartItem(@Param("userEmail") String userEmail, @Param("productSlug") String productSlug,
                            @Param("quantity") Integer quantity);

    @Modifying
    @Transactional
    @Query(value = "UPDATE cart_item c " +
            "JOIN product p ON c.product_id = p.id " +
            "JOIN app_user u ON c.user_id = u.id " +
            "SET c.quantity = :quantity " +
            "WHERE p.slug = :productSlug " +
            "AND u.email = :userEmail", nativeQuery = true)
    void updateUserCartItem(@Param("userEmail") String userEmail, @Param("productSlug") String productSlug,
                            @Param("quantity") Integer quantity);

    @Modifying
    @Transactional
    @Query(value = "DELETE c FROM cart_item c " +
            "JOIN product p ON c.product_id = p.id " +
            "JOIN app_user u ON c.user_id = u.id " +
            "WHERE p.slug = :productSlug " +
            "AND u.email = :userEmail", nativeQuery = true)
    void deleteUserCartItem(@Param("userEmail") String userEmail, @Param("productSlug") String productSlug);
}
