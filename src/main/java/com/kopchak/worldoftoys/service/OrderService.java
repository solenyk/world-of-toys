package com.kopchak.worldoftoys.service;

import com.kopchak.worldoftoys.dto.admin.product.order.FilteredOrdersPageDto;
import com.kopchak.worldoftoys.dto.admin.product.order.FilteringOrderOptionsDto;
import com.kopchak.worldoftoys.dto.admin.product.order.StatusDto;
import com.kopchak.worldoftoys.dto.order.OrderDto;
import com.kopchak.worldoftoys.dto.order.OrderRecipientDto;
import com.kopchak.worldoftoys.exception.exception.OrderCreationException;
import com.kopchak.worldoftoys.model.order.OrderStatus;
import com.kopchak.worldoftoys.model.order.payment.PaymentStatus;
import com.kopchak.worldoftoys.model.user.AppUser;

import java.util.List;
import java.util.Set;

public interface OrderService {
    void createOrder(OrderRecipientDto orderRecipientDto, AppUser user) throws OrderCreationException;

    Set<OrderDto> getAllUserOrders(AppUser user);

    FilteringOrderOptionsDto getOrderFilteringOptions();

    FilteredOrdersPageDto filterOrdersByStatusesAndDate(int pageNumber, int pageSize, List<OrderStatus> orderStatuses,
                                                        List<PaymentStatus> paymentStatuses, String dateSortOrder);
    void updateOrderStatus(String orderId, StatusDto statusDto) throws OrderCreationException;

    Set<StatusDto> getAllOrderStatuses();
}
