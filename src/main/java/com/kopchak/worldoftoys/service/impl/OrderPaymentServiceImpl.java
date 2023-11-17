package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.order.OrderRecipientDto;
import com.kopchak.worldoftoys.mapper.order.OrderRecipientMapper;
import com.kopchak.worldoftoys.model.order.OrderDetails;
import com.kopchak.worldoftoys.model.order.OrderStatus;
import com.kopchak.worldoftoys.model.order.recipient.OrderRecipient;
import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.model.user.AppUser;
import com.kopchak.worldoftoys.repository.cart.CartItemRepository;
import com.kopchak.worldoftoys.repository.order.OrderRepository;
import com.kopchak.worldoftoys.service.OrderPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderPaymentServiceImpl implements OrderPaymentService {
    private final OrderRecipientMapper orderRecipientMapper;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;

    public void createOrder(OrderRecipientDto orderRecipientDto, AppUser user){
        OrderRecipient orderRecipient = orderRecipientMapper.toOrderRecipient(orderRecipientDto);
        Set<Product> products = cartItemRepository.findAllProductsByUser(user);
        OrderDetails order = OrderDetails
                .builder()
                .orderStatus(OrderStatus.AWAITING_PAYMENT)
                .dateTime(LocalDateTime.now())
                .orderRecipient(orderRecipient)
                .user(user)
                .products(products)
                .build();
        orderRepository.save(order);
        cartItemRepository.deleteAllById_User(user);
    }
}
