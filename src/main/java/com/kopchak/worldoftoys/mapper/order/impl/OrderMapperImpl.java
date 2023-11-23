package com.kopchak.worldoftoys.mapper.order.impl;

import com.kopchak.worldoftoys.dto.order.OrderDto;
import com.kopchak.worldoftoys.dto.order.OrderProductDto;
import com.kopchak.worldoftoys.mapper.order.OrderMapper;
import com.kopchak.worldoftoys.model.cart.CartItem;
import com.kopchak.worldoftoys.model.order.Order;
import com.kopchak.worldoftoys.model.order.details.OrderDetails;
import com.kopchak.worldoftoys.model.product.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class OrderMapperImpl implements OrderMapper {
    public Set<OrderDetails> toOrderDetails(Set<CartItem> cartItems, Order order) {
        return cartItems
                .stream()
                .map(cartItem -> new OrderDetails(order, cartItem.getId().getProduct(), cartItem.getQuantity()))
                .collect(Collectors.toSet());
    }

    public Set<OrderDto> toOrderDtoSet(Set<Order> orders) {
        return orders.stream().map(order -> {
            Set<OrderProductDto> orderProductDtos = toOrderProductDtoSet(order.getOrderDetails());
            return OrderDto
                    .builder()
                    .id(order.getId())
                    .orderStatus(order.getOrderStatus())
                    .dateTime(order.getDateTime())
                    .products(orderProductDtos)
                    .totalPrice(calculateTotalPrice(orderProductDtos))
                    .build();
        }).collect(Collectors.toSet());
    }

    private Set<OrderProductDto> toOrderProductDtoSet(Set<OrderDetails> orderDetails) {
        return orderDetails.stream().map(orderDetail -> {
            Product product = orderDetail.getProduct();
            int quantity = orderDetail.getQuantity();
            BigDecimal price = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            return new OrderProductDto(product.getName(), price, quantity);
        }).collect(Collectors.toSet());
    }

    private BigDecimal calculateTotalPrice(Set<OrderProductDto> orderProductDtos) {
        return orderProductDtos.stream()
                .map(OrderProductDto::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
