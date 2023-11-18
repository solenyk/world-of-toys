package com.kopchak.worldoftoys.mapper.order;

import com.kopchak.worldoftoys.dto.order.OrderDto;
import com.kopchak.worldoftoys.model.cart.CartItem;
import com.kopchak.worldoftoys.model.order.Order;
import com.kopchak.worldoftoys.model.order.details.OrderDetails;

import java.util.Set;

public interface OrderMapper {
   Set<OrderDetails> toOrderDetails(Set<CartItem> cartItems, Order order);
   Set<OrderDto> toOrderDtoSet(Set<Order> orders);
}
