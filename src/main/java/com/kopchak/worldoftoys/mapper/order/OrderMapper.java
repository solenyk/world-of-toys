package com.kopchak.worldoftoys.mapper.order;

import com.kopchak.worldoftoys.dto.admin.product.order.FilteredOrdersPageDto;
import com.kopchak.worldoftoys.dto.admin.product.order.FilteringOrderOptionsDto;
import com.kopchak.worldoftoys.dto.admin.product.order.StatusDto;
import com.kopchak.worldoftoys.dto.order.OrderDto;
import com.kopchak.worldoftoys.exception.OrderCreationException;
import com.kopchak.worldoftoys.domain.cart.CartItem;
import com.kopchak.worldoftoys.domain.order.Order;
import com.kopchak.worldoftoys.domain.order.OrderStatus;
import com.kopchak.worldoftoys.domain.order.details.OrderDetails;
import com.kopchak.worldoftoys.domain.order.payment.PaymentStatus;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;

public interface OrderMapper {
    Set<OrderDetails> toOrderDetails(Set<CartItem> cartItems, Order order);

    Set<OrderDto> toOrderDtoSet(Set<Order> orders);

    FilteredOrdersPageDto toFilteredOrdersPageDto(Page<Order> orderPage);

    FilteringOrderOptionsDto toFilteringOrderOptionsDto(Set<OrderStatus> orderStatusSet,
                                                        Set<PaymentStatus> paymentStatusesSet);

    OrderStatus toOrderStatus(StatusDto statusDto) throws OrderCreationException;

    Set<StatusDto> toStatusDtoSet(List<OrderStatus> orderStatuses);
}
