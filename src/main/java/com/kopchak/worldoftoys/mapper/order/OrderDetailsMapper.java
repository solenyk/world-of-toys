package com.kopchak.worldoftoys.mapper.order;

import com.kopchak.worldoftoys.model.cart.CartItem;
import com.kopchak.worldoftoys.model.order.Order;
import com.kopchak.worldoftoys.model.order.details.OrderDetails;
import org.mapstruct.Mapper;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface OrderDetailsMapper {
    default Set<OrderDetails> toOrderDetails(Set<CartItem> cartItems, Order order) {
        return cartItems
                .stream()
                .map(cartItem -> new OrderDetails(order, cartItem.getId().getProduct(), cartItem.getQuantity()))
                .collect(Collectors.toSet());
    }
}
