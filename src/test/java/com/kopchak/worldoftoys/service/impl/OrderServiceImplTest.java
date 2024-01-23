package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.domain.cart.CartItem;
import com.kopchak.worldoftoys.domain.cart.CartItemId;
import com.kopchak.worldoftoys.domain.order.Order;
import com.kopchak.worldoftoys.domain.order.OrderStatus;
import com.kopchak.worldoftoys.domain.order.details.OrderDetails;
import com.kopchak.worldoftoys.domain.order.payment.PaymentStatus;
import com.kopchak.worldoftoys.domain.order.recipient.OrderRecipient;
import com.kopchak.worldoftoys.domain.product.Product;
import com.kopchak.worldoftoys.domain.user.AppUser;
import com.kopchak.worldoftoys.dto.admin.product.order.FilteredOrdersPageDto;
import com.kopchak.worldoftoys.dto.admin.product.order.FilteringOrderOptionsDto;
import com.kopchak.worldoftoys.dto.admin.product.order.StatusDto;
import com.kopchak.worldoftoys.dto.order.OrderDto;
import com.kopchak.worldoftoys.dto.order.OrderRecipientDto;
import com.kopchak.worldoftoys.exception.InvalidOrderException;
import com.kopchak.worldoftoys.exception.InvalidOrderStatusException;
import com.kopchak.worldoftoys.exception.MessageSendingException;
import com.kopchak.worldoftoys.exception.OrderCreationException;
import com.kopchak.worldoftoys.mapper.order.OrderMapper;
import com.kopchak.worldoftoys.mapper.order.OrderRecipientMapper;
import com.kopchak.worldoftoys.repository.cart.CartItemRepository;
import com.kopchak.worldoftoys.repository.order.OrderDetailsRepository;
import com.kopchak.worldoftoys.repository.order.OrderRepository;
import com.kopchak.worldoftoys.repository.product.ProductRepository;
import com.kopchak.worldoftoys.repository.specifications.OrderSpecifications;
import com.kopchak.worldoftoys.service.EmailSenderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    @Mock
    private OrderRecipientMapper orderRecipientMapper;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderDetailsRepository orderDetailsRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderSpecifications orderSpecifications;
    @Mock
    private EmailSenderService emailSenderService;
    @InjectMocks
    private OrderServiceImpl orderService;

    private final static String ORDER_ID = "order-id";
    private final static String USER_EMAIL = "user@test.com";
    private final String USER_FIRSTNAME = "Firstname";
    private final static OrderStatus NEW_ORDER_STATUS = OrderStatus.CANCELED;

    private AppUser user;
    private OrderRecipientDto orderRecipientDto;
    private Order order;
    private StatusDto statusDto;

    @BeforeEach
    void setUp() {
        user = AppUser
                .builder()
                .email(USER_EMAIL)
                .firstname(USER_FIRSTNAME)
                .build();
        orderRecipientDto = OrderRecipientDto.builder().build();
        order = Order
                .builder()
                .id(ORDER_ID)
                .orderStatus(OrderStatus.AWAITING_PAYMENT)
                .user(user)
                .build();
        statusDto = new StatusDto(NEW_ORDER_STATUS.name(), NEW_ORDER_STATUS.getStatus());
    }

    @Test
    public void createOrder_CartIsNotEmpty() throws OrderCreationException {
        Product product = Product.builder().availableQuantity(BigInteger.TWO).build();
        CartItem cartItem = CartItem.builder().id(new CartItemId(user, product)).quantity(BigInteger.ONE).build();
        OrderRecipient orderRecipient = OrderRecipient.builder().build();
        Set<OrderDetails> expectedOrderDetailsSet = Set.of(OrderDetails.builder().build());

        when(cartItemRepository.calculateUserCartTotalPrice(eq(user))).thenReturn(BigDecimal.valueOf(1000));
        when(cartItemRepository.deleteAllById_User(eq(user))).thenReturn(Set.of(cartItem));
        when(orderRecipientMapper.toOrderRecipient(eq(orderRecipientDto))).thenReturn(orderRecipient);
        when(orderMapper.toOrderDetails(anySet(), any())).thenReturn(expectedOrderDetailsSet);

        orderService.createOrder(orderRecipientDto, user);

        verify(orderRepository).save(any(Order.class));
        verify(orderDetailsRepository).saveAll(eq(expectedOrderDetailsSet));
        assertThat(product.getAvailableQuantity()).isEqualTo(BigInteger.ONE);
    }

    @Test
    public void createOrder_CartIsEmpty_ThrowsOrderCreationException() {
        String orderCreationExceptionMsg = "It is impossible to create an order for the user " +
                "because there are no products in the user's cart.";

        when(cartItemRepository.deleteAllById_User(user)).thenReturn(new HashSet<>());

        assertException(OrderCreationException.class, orderCreationExceptionMsg,
                () -> orderService.createOrder(orderRecipientDto, user));
    }

    @Test
    public void getAllUserOrders_ReturnsOrderDtoSet() {
        Set<Order> expectedOrderSet = Set.of(Order.builder().build());
        Set<OrderDto> expectedOrderDtoSet = Set.of(OrderDto.builder().build());

        when(orderRepository.findAllByUser(eq(user))).thenReturn(expectedOrderSet);
        when(orderMapper.toOrderDtoSet(eq(expectedOrderSet))).thenReturn(expectedOrderDtoSet);

        Set<OrderDto> actualOrderDtoSet = orderService.getAllUserOrders(user);

        assertThat(actualOrderDtoSet).isNotNull();
        assertThat(actualOrderDtoSet).isNotEmpty();
        assertThat(actualOrderDtoSet).isEqualTo(expectedOrderDtoSet);
    }

    @Test
    public void getOrderFilteringOptions() {
        OrderStatus orderStatus = OrderStatus.AWAITING_PAYMENT;
        PaymentStatus paymentStatus = PaymentStatus.COMPLETE;
        Set<OrderStatus> orderStatusSet = Set.of(orderStatus);
        Set<PaymentStatus> paymentStatusSet = Set.of(paymentStatus);
        var expectedFilteringOrderOptionsDto = new FilteringOrderOptionsDto(
                Set.of(new StatusDto(orderStatus.name(), orderStatus.getStatus())),
                Set.of(new StatusDto(paymentStatus.name(), paymentStatus.getStatus()))
        );

        when(orderRepository.findAllOrderStatuses()).thenReturn(orderStatusSet);
        when(orderRepository.findAllPaymentStatuses()).thenReturn(paymentStatusSet);
        when(orderMapper.toFilteringOrderOptionsDto(eq(orderStatusSet), eq(paymentStatusSet)))
                .thenReturn(expectedFilteringOrderOptionsDto);

        var actualFilteringOrderOptionsDto = orderService.getOrderFilteringOptions();

        assertThat(actualFilteringOrderOptionsDto).isNotNull();
        assertThat(actualFilteringOrderOptionsDto).isEqualTo(expectedFilteringOrderOptionsDto);
    }

    @Test
    public void filterOrdersByStatusesAndDate() {
        int pageNumber = 0;
        int pageSize = 10;
        String dateSortOrder = "asc";
        Specification<Order> mockSpecification = mock(Specification.class);
        var expectedFilteredOrdersPageDto = new FilteredOrdersPageDto(new HashSet<>(), 0L, 0L);

        when(orderSpecifications.filterByStatusesAndDate(any(), any(), eq(dateSortOrder))).thenReturn(mockSpecification);
        when(orderRepository.findAll(eq(mockSpecification), any(Pageable.class))).thenReturn(Page.empty());
        when(orderMapper.toFilteredOrdersPageDto(any())).thenReturn(expectedFilteredOrdersPageDto);

        var actualFilteredOrdersPageDto = orderService.filterOrdersByStatusesAndDate(pageNumber, pageSize, null,
                null, dateSortOrder);

        assertThat(actualFilteredOrdersPageDto).isNotNull();
        assertThat(actualFilteredOrdersPageDto).isEqualTo(expectedFilteredOrdersPageDto);
    }

    @Test
    public void updateOrderStatus_ExistentOrderId() throws InvalidOrderStatusException, MessageSendingException, InvalidOrderException {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderMapper.toOrderStatus(eq(statusDto))).thenReturn(NEW_ORDER_STATUS);
        doNothing().when(emailSenderService)
                .sendEmail(eq(USER_EMAIL), eq(USER_FIRSTNAME), eq(ORDER_ID), eq(NEW_ORDER_STATUS));

        orderService.updateOrderStatus(ORDER_ID, statusDto);

        verify(orderRepository).save(order);
        verify(emailSenderService).sendEmail(eq(USER_EMAIL), eq(USER_FIRSTNAME), eq(ORDER_ID),
                eq(NEW_ORDER_STATUS));

        assertThat(order.getOrderStatus()).isEqualTo(NEW_ORDER_STATUS);
    }

    @Test
    public void updateOrderStatus_NonExistentOrderId_ThrowInvalidOrderException() throws MessageSendingException {
        String invalidOrderExceptionMsg = String.format("Order with id: %s doesn't exist!", ORDER_ID);

        assertException(InvalidOrderException.class, invalidOrderExceptionMsg,
                () -> orderService.updateOrderStatus(ORDER_ID, statusDto));

        verify(orderRepository, never()).save(order);
        verify(emailSenderService, never()).sendEmail(eq(USER_EMAIL), eq(USER_FIRSTNAME), eq(ORDER_ID),
                eq(NEW_ORDER_STATUS));

        assertThat(order.getOrderStatus()).isNotEqualTo(NEW_ORDER_STATUS);
    }

    @Test
    public void updateOrderStatus_InvalidNewOrderStatus_ThrowInvalidOrderStatusException() throws MessageSendingException, InvalidOrderStatusException {
        order.setOrderStatus(NEW_ORDER_STATUS);
        String invalidOrderStatusExceptionMsg = String.format("The status: %s of the order with id: %s " +
                "is the same as the current status", statusDto.status(), ORDER_ID);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderMapper.toOrderStatus(eq(statusDto))).thenReturn(NEW_ORDER_STATUS);

        assertException(InvalidOrderStatusException.class, invalidOrderStatusExceptionMsg,
                () -> orderService.updateOrderStatus(ORDER_ID, statusDto));

        verify(orderRepository, never()).save(order);
        verify(emailSenderService, never()).sendEmail(eq(USER_EMAIL), eq(USER_FIRSTNAME), eq(ORDER_ID),
                eq(NEW_ORDER_STATUS));
    }

    @Test
    public void getAllOrderStatuses_ReturnsStatusDtoSet() {
        var orderStatusesList = Arrays.asList(OrderStatus.values());
        var expectedStatusDtoSet = Set.of(statusDto);

        when(orderMapper.toStatusDtoSet(eq(orderStatusesList))).thenReturn(expectedStatusDtoSet);

        var actualStatusDtoSet = orderService.getAllOrderStatuses();

        assertThat(actualStatusDtoSet).isNotNull();
        assertThat(actualStatusDtoSet).isNotEmpty();
        assertThat(actualStatusDtoSet).isEqualTo(expectedStatusDtoSet);
    }

    private void assertException(Class<? extends Exception> expectedExceptionType,
                                 String expectedMessage, Executable executable) {
        Exception exception = assertThrows(expectedExceptionType, executable);
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }
}