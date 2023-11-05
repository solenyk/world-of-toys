package com.kopchak.worldoftoys.repository.cart;

import com.kopchak.worldoftoys.model.cart.CartItem;
import com.kopchak.worldoftoys.model.cart.CartItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, CartItemId> {

}
