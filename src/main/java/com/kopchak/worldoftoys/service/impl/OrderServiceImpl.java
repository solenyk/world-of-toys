package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.order.OrderDto;
import com.kopchak.worldoftoys.dto.order.OrderRecipientDto;
import com.kopchak.worldoftoys.exception.exception.OrderException;
import com.kopchak.worldoftoys.mapper.order.OrderMapper;
import com.kopchak.worldoftoys.mapper.order.OrderRecipientMapper;
import com.kopchak.worldoftoys.model.cart.CartItem;
import com.kopchak.worldoftoys.model.order.Order;
import com.kopchak.worldoftoys.model.order.OrderStatus;
import com.kopchak.worldoftoys.model.order.recipient.OrderRecipient;
import com.kopchak.worldoftoys.model.user.AppUser;
import com.kopchak.worldoftoys.repository.cart.CartItemRepository;
import com.kopchak.worldoftoys.repository.order.OrderDetailsRepository;
import com.kopchak.worldoftoys.repository.order.OrderRepository;
import com.kopchak.worldoftoys.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRecipientMapper orderRecipientMapper;
    private final OrderMapper orderMapper;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailsRepository orderDetailsRepository;

    public void createOrder(OrderRecipientDto orderRecipientDto, AppUser user) throws OrderException {
        Set<CartItem> cartItems = cartItemRepository.deleteAllById_User(user);
        if (cartItems.isEmpty()) {
            throw new OrderException(String.format("Impossible to create an order for the user: %s " +
                    "because there are no products in the user's cart.", user.getUsername()));
        }
        OrderRecipient orderRecipient = orderRecipientMapper.toOrderRecipient(orderRecipientDto);
        Order order = Order
                .builder()
                .orderStatus(OrderStatus.AWAITING_PAYMENT)
                .dateTime(LocalDateTime.now())
                .orderRecipient(orderRecipient)
                .user(user)
                .build();
        order = orderRepository.save(order);
        var orderDetails = orderMapper.toOrderDetails(cartItems, order);
        orderDetailsRepository.saveAll(orderDetails);
    }

    public Set<OrderDto> getAllUserOrders(AppUser user) {
        Set<Order> orders = orderRepository.findAllByUser(user);
        return orderMapper.toOrderDtoSet(orders);
    }
}
