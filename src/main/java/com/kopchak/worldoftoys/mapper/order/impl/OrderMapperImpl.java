package com.kopchak.worldoftoys.mapper.order.impl;

import com.kopchak.worldoftoys.dto.admin.product.order.*;
import com.kopchak.worldoftoys.dto.order.OrderDto;
import com.kopchak.worldoftoys.dto.order.OrderProductDto;
import com.kopchak.worldoftoys.exception.OrderCreationException;
import com.kopchak.worldoftoys.mapper.order.OrderMapper;
import com.kopchak.worldoftoys.model.cart.CartItem;
import com.kopchak.worldoftoys.model.order.Order;
import com.kopchak.worldoftoys.model.order.OrderStatus;
import com.kopchak.worldoftoys.model.order.StatusProvider;
import com.kopchak.worldoftoys.model.order.details.OrderDetails;
import com.kopchak.worldoftoys.model.order.payment.Payment;
import com.kopchak.worldoftoys.model.order.payment.PaymentStatus;
import com.kopchak.worldoftoys.model.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class OrderMapperImpl implements OrderMapper {
    @Override
    public Set<OrderDetails> toOrderDetails(Set<CartItem> cartItems, Order order) {
        return cartItems
                .stream()
                .map(cartItem -> new OrderDetails(order, cartItem.getId().getProduct(), cartItem.getQuantity()))
                .collect(Collectors.toSet());
    }

    @Override
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

    @Override
    public FilteredOrdersPageDto toFilteredOrdersPageDto(Page<Order> orderPage) {
        return new FilteredOrdersPageDto(toAdminOrderDto(orderPage.getContent()),
                orderPage.getTotalElements(), orderPage.getTotalPages());
    }

    @Override
    public FilteringOrderOptionsDto toFilteringOrderOptionsDto(Set<OrderStatus> orderStatusSet,
                                                               Set<PaymentStatus> paymentStatusesSet) {
        return new FilteringOrderOptionsDto(toStatusDtoSet(orderStatusSet), toStatusDtoSet(paymentStatusesSet));
    }

    @Override
    public OrderStatus toOrderStatus(StatusDto statusDto) throws OrderCreationException {
        try {
            return OrderStatus.valueOf(statusDto.name());
        } catch (IllegalArgumentException e) {
            throw new OrderCreationException(String.format("Order status with name: %s doesn't exist!", statusDto.status()));
        }
    }

    @Override
    public Set<StatusDto> toStatusDtoSet(List<OrderStatus> orderStatuses) {
        return orderStatuses
                .stream()
                .map(orderStatus -> new StatusDto(orderStatus.name(), orderStatus.getStatus()))
                .collect(Collectors.toSet());
    }

    private <T extends Enum<T> & StatusProvider> Set<StatusDto> toStatusDtoSet(Set<T> statuses) {
        return statuses.stream()
                .filter(Objects::nonNull)
                .map(status -> new StatusDto(status.getStatus(), status.name()))
                .collect(Collectors.toSet());
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

    private Set<AdminOrderDto> toAdminOrderDto(List<Order> orders) {
        return orders.stream().map(order -> {
            Set<OrderProductDto> products = toOrderProductDtoSet(order.getOrderDetails());
            Set<PaymentDto> payments = toPaymentDtoSet(order.getPayments());
            return AdminOrderDto
                    .builder()
                    .id(order.getId())
                    .orderStatus(order.getOrderStatus())
                    .dateTime(order.getDateTime())
                    .products(products)
                    .payments(payments)
                    .totalPrice(calculateTotalPrice(products))
                    .build();
        }).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<PaymentDto> toPaymentDtoSet(Set<Payment> payments) {
        return payments.stream().map(payment -> new PaymentDto(payment.getStatus(), payment.getDateTime()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
