package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.domain.cart.CartItem;
import com.kopchak.worldoftoys.domain.order.Order;
import com.kopchak.worldoftoys.domain.order.details.OrderDetails;
import com.kopchak.worldoftoys.domain.order.recipient.OrderRecipient;
import com.kopchak.worldoftoys.domain.user.AppUser;
import com.kopchak.worldoftoys.dto.order.OrderDto;
import com.kopchak.worldoftoys.dto.order.OrderRecipientDto;
import com.kopchak.worldoftoys.exception.OrderCreationException;
import com.kopchak.worldoftoys.mapper.order.OrderMapper;
import com.kopchak.worldoftoys.mapper.order.OrderRecipientMapper;
import com.kopchak.worldoftoys.repository.cart.CartItemRepository;
import com.kopchak.worldoftoys.repository.order.OrderDetailsRepository;
import com.kopchak.worldoftoys.repository.order.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    @InjectMocks
    private OrderServiceImpl orderService;

    private AppUser user;
    private OrderRecipientDto orderRecipientDto;

    @BeforeEach
    void setUp() {
        user = new AppUser();
        orderRecipientDto = OrderRecipientDto.builder().build();
    }

    @Test
    public void createOrder_CartIsNotEmpty() throws OrderCreationException {
        OrderRecipient orderRecipient = OrderRecipient.builder().build();
        Set<OrderDetails> expectedOrderDetailsSet = Set.of(OrderDetails.builder().build());

        when(cartItemRepository.deleteAllById_User(user)).thenReturn(Set.of(new CartItem()));
        when(orderRecipientMapper.toOrderRecipient(eq(orderRecipientDto))).thenReturn(orderRecipient);
        when(orderMapper.toOrderDetails(anySet(), any())).thenReturn(expectedOrderDetailsSet);

        orderService.createOrder(orderRecipientDto, user);

        verify(orderRepository).save(any(Order.class));

        ArgumentCaptor<Set<OrderDetails>> orderDetailsArgumentCaptor = ArgumentCaptor.forClass(Set.class);
        verify(orderDetailsRepository).saveAll(orderDetailsArgumentCaptor.capture());
        Set<OrderDetails> actualOrderDetailsSet = orderDetailsArgumentCaptor.getValue();

        assertThat(actualOrderDetailsSet).isNotNull();
        assertThat(actualOrderDetailsSet).isNotEmpty();
        assertThat(actualOrderDetailsSet).isEqualTo(expectedOrderDetailsSet);
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
    public void getAllUserOrders() {
        Set<Order> expectedOrderSet = Set.of(Order.builder().build());
        Set<OrderDto> expectedOrderDtoSet = Set.of(OrderDto.builder().build());

        when(orderRepository.findAllByUser(eq(user))).thenReturn(expectedOrderSet);
        when(orderMapper.toOrderDtoSet(eq(expectedOrderSet))).thenReturn(expectedOrderDtoSet);

        Set<OrderDto> actualOrderDtoSet = orderService.getAllUserOrders(user);

        assertThat(actualOrderDtoSet).isNotNull();
        assertThat(actualOrderDtoSet).isNotEmpty();
        assertThat(actualOrderDtoSet).isEqualTo(expectedOrderDtoSet);
    }

    private void assertException(Class<? extends Exception> expectedExceptionType,
                                 String expectedMessage, Executable executable) {
        Exception exception = assertThrows(expectedExceptionType, executable);
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }
}