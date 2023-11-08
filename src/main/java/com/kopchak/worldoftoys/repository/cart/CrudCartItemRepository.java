package com.kopchak.worldoftoys.repository.cart;

import com.kopchak.worldoftoys.model.cart.CartItem;
import com.kopchak.worldoftoys.model.cart.CartItemId;
import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.model.user.AppUser;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CrudCartItemRepository {
    private final EntityManager entityManager;

    @Modifying
    @Transactional
    public void insertOrUpdateCartItem(String userEmail, String productSlug, Integer quantity){
        Optional<CartItem> optionalCartItem = getCartItemByUserEmailAndProductSlug(userEmail, productSlug);
        if(optionalCartItem.isPresent()){
            CartItem cartItem = optionalCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            entityManager.merge(cartItem);
        } else {
            Product product = entityManager.createQuery(
                    "SELECT p FROM Product p WHERE p.slug = :productSlug", Product.class)
                    .setParameter("productSlug", productSlug).getSingleResult();
            AppUser appUser =
                    entityManager.createQuery(
                            "SELECT u FROM AppUser u WHERE u.email = :userEmail", AppUser.class)
                    .setParameter("userEmail", userEmail).getSingleResult();
            CartItemId cartItemId = new CartItemId(appUser, product);
            CartItem cartItem = new CartItem(cartItemId, quantity);
            entityManager.persist(cartItem);
        }
        entityManager.flush();
    }

    private Optional<CartItem> getCartItemByUserEmailAndProductSlug(String userEmail, String productSlug){
        CartItem cartItem = null;
        try {
            cartItem = entityManager.createQuery(
                    "SELECT c FROM CartItem c " +
                            "JOIN c.id.user u " +
                            "JOIN c.id.product p " +
                            "WHERE u.email = :userEmail AND p.slug = :productSlug",
                    CartItem.class
            ).setParameter("userEmail", userEmail).setParameter("productSlug", productSlug).getSingleResult();
        } catch (NoResultException e) {
            log.info("Cart item with user email: {} and product slug: {} doesn't exist", userEmail, productSlug);
        }
        return Optional.ofNullable(cartItem);
    }
}
