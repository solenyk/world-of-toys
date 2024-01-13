package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.admin.product.order.FilteredOrdersPageDto;
import com.kopchak.worldoftoys.dto.admin.product.order.FilteringOrderOptionsDto;
import com.kopchak.worldoftoys.dto.admin.product.order.StatusDto;
import com.kopchak.worldoftoys.dto.order.OrderDto;
import com.kopchak.worldoftoys.dto.order.OrderRecipientDto;
import com.kopchak.worldoftoys.exception.OrderCreationException;
import com.kopchak.worldoftoys.mapper.order.OrderMapper;
import com.kopchak.worldoftoys.mapper.order.OrderRecipientMapper;
import com.kopchak.worldoftoys.model.cart.CartItem;
import com.kopchak.worldoftoys.model.order.Order;
import com.kopchak.worldoftoys.model.order.OrderStatus;
import com.kopchak.worldoftoys.model.order.payment.PaymentStatus;
import com.kopchak.worldoftoys.model.order.recipient.OrderRecipient;
import com.kopchak.worldoftoys.model.user.AppUser;
import com.kopchak.worldoftoys.repository.cart.CartItemRepository;
import com.kopchak.worldoftoys.repository.order.OrderDetailsRepository;
import com.kopchak.worldoftoys.repository.order.OrderRepository;
import com.kopchak.worldoftoys.repository.specifications.OrderSpecifications;
import com.kopchak.worldoftoys.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRecipientMapper orderRecipientMapper;
    private final OrderMapper orderMapper;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailsRepository orderDetailsRepository;
    private final OrderSpecifications orderSpecifications;

    @Override
    public void createOrder(OrderRecipientDto orderRecipientDto, AppUser user) throws OrderCreationException {
        Set<CartItem> cartItems = cartItemRepository.deleteAllById_User(user);
        String username = user.getUsername();
        if (cartItems.isEmpty()) {
            throw new OrderCreationException("It is impossible to create an order for the user " +
                    "because there are no products in the user's cart.");
        }
        OrderRecipient orderRecipient = orderRecipientMapper.toOrderRecipient(orderRecipientDto);
        Order order = Order
                .builder()
                .orderRecipient(orderRecipient)
                .user(user)
                .build();
        order = orderRepository.save(order);
        var orderDetails = orderMapper.toOrderDetails(cartItems, order);
        orderDetailsRepository.saveAll(orderDetails);
        log.info("The order for user with username: {} has been successfully created.", username);
    }

    @Override
    public Set<OrderDto> getAllUserOrders(AppUser user) {
        Set<Order> orders = orderRepository.findAllByUser(user);
        return orderMapper.toOrderDtoSet(orders);
    }

    @Override
    public FilteringOrderOptionsDto getOrderFilteringOptions() {
        Set<OrderStatus> orderStatusSet = orderRepository.findAllOrderStatuses();
        Set<PaymentStatus> paymentStatusSet = orderRepository.findAllPaymentStatuses();
        return orderMapper.toFilteringOrderOptionsDto(orderStatusSet, paymentStatusSet);
    }

    @Override
    public FilteredOrdersPageDto filterOrdersByStatusesAndDate(int pageNumber, int pageSize, List<OrderStatus> orderStatuses,
                                                               List<PaymentStatus> paymentStatuses, String dateSortOrder) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Specification<Order> spec = orderSpecifications.filterByStatusesAndDate(orderStatuses, paymentStatuses,
                dateSortOrder);
        Page<Order> orderPage = orderRepository.findAll(spec, pageable);
        return orderMapper.toFilteredOrdersPageDto(orderPage);
    }

    @Override
    public void updateOrderStatus(String orderId, StatusDto statusDto) throws OrderCreationException {
        Order order = orderRepository.findById(orderId).orElseThrow(() ->
                new OrderCreationException(String.format("Order with id: %s doesn't exist!", orderId)));
        OrderStatus orderStatus = orderMapper.toOrderStatus(statusDto);
        if (!order.getOrderStatus().equals(orderStatus)) {
            throw new OrderCreationException(String.format("The status: %s of the order with id: %s " +
                    "is the same as the current status", statusDto.status(), orderId));
        }
        order.setOrderStatus(orderStatus);
        orderRepository.save(order);
        AppUser user = order.getUser();
        //ToDo:send email
    }

    @Override
    public Set<StatusDto> getAllOrderStatuses() {
        return orderMapper.toStatusDtoSet(Arrays.asList(OrderStatus.values()));
    }
}
