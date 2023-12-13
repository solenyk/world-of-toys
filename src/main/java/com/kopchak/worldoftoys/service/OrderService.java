package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.order.OrderDto;
import com.kopchak.worldoftoys.dto.order.OrderRecipientDto;
import com.kopchak.worldoftoys.exception.exception.OrderException;
import com.kopchak.worldoftoys.model.user.AppUser;

import java.util.Set;

public interface OrderService {
    void createOrder(OrderRecipientDto orderRecipientDto, AppUser user) throws OrderException;
    Set<OrderDto> getAllUserOrders(AppUser user);
}
