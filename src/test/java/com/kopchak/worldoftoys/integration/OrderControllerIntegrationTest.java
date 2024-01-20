package com.kopchak.worldoftoys.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kopchak.worldoftoys.dto.error.ResponseStatusExceptionDto;
import com.kopchak.worldoftoys.dto.order.*;
import com.kopchak.worldoftoys.domain.order.OrderStatus;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("integrationtest")
@Transactional
public class OrderControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    private OrderRecipientDto orderRecipientDto;
    private ResponseStatusExceptionDto accessDeniedErrorResponse;
    private ResponseStatusExceptionDto unauthorizedErrorResponse;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        orderRecipientDto = OrderRecipientDto
                .builder()
                .firstname("Firstname")
                .lastname("Lastname")
                .patronymic("Patronymic")
                .address(new AddressDto("region", "settlement", "street", 1, 1))
                .phoneNumber(new PhoneNumberDto("95", "1234567"))
                .build();
        accessDeniedErrorResponse = new ResponseStatusExceptionDto(HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.name(), "Access Denied");
        unauthorizedErrorResponse = new ResponseStatusExceptionDto(HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.name(), "Full authentication is required to access this resource");
    }

    @Test
    @WithUserDetails("john.doe@example.com")
    public void createOrder_AuthUser_ReturnsCreatedStatus() throws Exception {
        ResultActions response = mockMvc.perform(post("/api/v1/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRecipientDto))
                .with(csrf()));

        response.andExpect(MockMvcResultMatchers.status().isCreated())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("alice.johnson@example.com")
    public void createOrder_AuthUserWithEmptyCart_ReturnsBadRequestStatus() throws Exception {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        String orderCreationExceptionMsg =
                "It is impossible to create an order for the user because there are no products in the user's cart.";

        ResultActions response = mockMvc.perform(post("/api/v1/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRecipientDto))
                .with(csrf()));

        var responseStatusExceptionDto = new ResponseStatusExceptionDto(httpStatus.value(), httpStatus.name(),
                orderCreationExceptionMsg);

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    public void createOrder_AnonymousUser_ReturnsUnauthorizedStatus() throws Exception {
        ResultActions response = mockMvc.perform(post("/api/v1/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRecipientDto))
                .with(csrf()));

        response.andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().json(objectMapper.writeValueAsString(unauthorizedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("jane.smith@example.com")
    public void createOrder_AuthAdminUser_ReturnsForbiddenStatus() throws Exception {
        ResultActions response = mockMvc.perform(post("/api/v1/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRecipientDto))
                .with(csrf()));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().json(objectMapper.writeValueAsString(accessDeniedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("john.doe@example.com")
    public void getAllUserOrders_AuthUser_ReturnsOkStatusAndSetOfOrderDto() throws Exception {
        Set<OrderDto> returnedOrderDtoSet = Set.of(OrderDto
                .builder()
                .id("4c980930-16eb-41cd-b998-29d03118d67c")
                .dateTime(LocalDateTime.of(2023, 11, 19, 18, 9, 52))
                .orderStatus(OrderStatus.AWAITING_PAYMENT)
                .products(Set.of(new OrderProductDto("Лялька Клаймбер", "lyalka-klaymber",
                        BigInteger.valueOf(2))))
                .totalPrice(BigDecimal.valueOf(1700))
                .build());
        ResultActions response = mockMvc.perform(get("/api/v1/order")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(returnedOrderDtoSet)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    public void getAllUserOrders_AnonymousUser_ReturnsUnauthorizedStatus() throws Exception {
        ResultActions response = mockMvc.perform(get("/api/v1/order")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()));

        response.andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().json(objectMapper.writeValueAsString(unauthorizedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("jane.smith@example.com")
    public void getAllUserOrders_AuthAdminUser_ReturnsForbiddenStatus() throws Exception {
        ResultActions response = mockMvc.perform(get("/api/v1/order")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().json(objectMapper.writeValueAsString(accessDeniedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }
}
