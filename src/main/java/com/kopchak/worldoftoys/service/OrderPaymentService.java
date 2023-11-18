package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.order.OrderDto;
import com.kopchak.worldoftoys.dto.order.OrderRecipientDto;
import com.kopchak.worldoftoys.model.user.AppUser;

import java.util.Set;

public interface OrderPaymentService {
    void createOrder(OrderRecipientDto orderRecipientDto, AppUser user);
    Set<OrderDto> getAllUserOrders(AppUser user);
}
