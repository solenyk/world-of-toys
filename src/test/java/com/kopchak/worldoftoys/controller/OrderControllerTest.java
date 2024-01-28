package com.kopchak.worldoftoys.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kopchak.worldoftoys.domain.user.AppUser;
import com.kopchak.worldoftoys.dto.error.ResponseStatusExceptionDto;
import com.kopchak.worldoftoys.dto.order.AddressDto;
import com.kopchak.worldoftoys.dto.order.OrderDto;
import com.kopchak.worldoftoys.dto.order.OrderRecipientDto;
import com.kopchak.worldoftoys.dto.order.PhoneNumberDto;
import com.kopchak.worldoftoys.exception.exception.cart.CartValidationException;
import com.kopchak.worldoftoys.exception.exception.order.OrderCreationException;
import com.kopchak.worldoftoys.service.CartService;
import com.kopchak.worldoftoys.service.JwtTokenService;
import com.kopchak.worldoftoys.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(controllers = OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private CartService cartService;

    @MockBean
    private JwtTokenService jwtTokenService;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderRecipientDto orderRecipientDto;

    @BeforeEach
    void setUp() {
        orderRecipientDto = OrderRecipientDto
                .builder()
                .firstname("Firstname")
                .lastname("Lastname")
                .patronymic("Patronymic")
                .address(new AddressDto("region", "settlement", "street", 1, 1))
                .phoneNumber(new PhoneNumberDto("95", "1234567"))
                .build();
    }

    @Test
    public void verifyCartBeforeOrderCreation_ReturnsOkStatus() throws Exception {
        doNothing().when(cartService).verifyCartBeforeOrderCreation(any(AppUser.class));

        ResultActions response = mockMvc.perform(get("/api/v1/order/verify-cart")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void verifyCartBeforeOrderCreation_ThrowCartValidationException_ReturnsBadRequestStatusAndResponseStatusExceptionDto() throws Exception {
        String cartValidationExceptionMsg = "Some products in the cart are not available in the selected quantity " +
                "because one or more products are out of stock";
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        var responseStatusExceptionDto =
                new ResponseStatusExceptionDto(httpStatus.value(), httpStatus.name(), cartValidationExceptionMsg);

        doThrow(new CartValidationException(cartValidationExceptionMsg))
                .when(cartService).verifyCartBeforeOrderCreation(any());

        ResultActions response = mockMvc.perform(get("/api/v1/order/verify-cart")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void createOrder_ReturnsCreatedStatus() throws Exception {
        doNothing().when(orderService).createOrder(eq(orderRecipientDto), any(AppUser.class));

        ResultActions response = mockMvc.perform(post("/api/v1/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRecipientDto)));

        response.andExpect(MockMvcResultMatchers.status().isCreated())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void createOrder_ThrowOrderCreationException_ReturnsBadRequestStatusAndResponseStatusExceptionDto() throws Exception {
        String orderCreationExceptionMsg = "It is impossible to create an order for the user " +
                "because there are no products in the user's cart.";
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        var responseStatusExceptionDto =
                new ResponseStatusExceptionDto(httpStatus.value(), httpStatus.name(), orderCreationExceptionMsg);

        doThrow(new OrderCreationException(orderCreationExceptionMsg))
                .when(orderService).createOrder(eq(orderRecipientDto), any());

        ResultActions response = mockMvc.perform(post("/api/v1/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRecipientDto)));

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void getAllUserOrders_ReturnsOkStatusAndSetOfOrderDto() throws Exception {
        Set<OrderDto> returnedOrderDtoSet = Set.of(OrderDto.builder().build());
        when(orderService.getAllUserOrders(any())).thenReturn(returnedOrderDtoSet);

        ResultActions response = mockMvc.perform(get("/api/v1/order")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(returnedOrderDtoSet)))
                .andDo(MockMvcResultHandlers.print());
    }
}