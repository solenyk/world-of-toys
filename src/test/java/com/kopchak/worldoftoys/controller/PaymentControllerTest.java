package com.kopchak.worldoftoys.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kopchak.worldoftoys.dto.error.ExceptionDto;
import com.kopchak.worldoftoys.dto.payment.StripeCredentialsDto;
import com.kopchak.worldoftoys.exception.exception.email.MessageSendingException;
import com.kopchak.worldoftoys.exception.exception.order.OrderNotFoundException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.StripeException;
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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@WebMvcTest(controllers = PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private JwtTokenService jwtTokenService;

    @Autowired
    private ObjectMapper objectMapper;

    private final static String ORDER_ID = "order_id";
    private static final String STRIPE_HEADER = "Stripe-Signature";
    private StripeCredentialsDto credentialsDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        credentialsDto = new StripeCredentialsDto("user", "user@test.com");
    }

    @Test
    public void stripeCheckout_ReturnsFoundStatusAndRedirectsToStripeCheckoutPage() throws Exception {
        String expectedStripeCheckoutUserUrl = "stripe_checkout_url";

        when(paymentService.stripeCheckout(credentialsDto, ORDER_ID)).thenReturn(expectedStripeCheckoutUserUrl);

        ResultActions response = mockMvc.perform(post("/api/v1/payment/{orderId}", ORDER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentialsDto)));

        response.andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrl(expectedStripeCheckoutUserUrl))
                .andDo(print());
    }

    @Test
    public void stripeCheckout_ThrowStripeException_ReturnsForbiddenStatusAndResponseStatusExceptionDto() throws Exception {
        StripeException stripeException = new AuthenticationException("msg", "code", "request-id", 403);

        when(paymentService.stripeCheckout(credentialsDto, ORDER_ID)).thenThrow(stripeException);

        ResultActions response = mockMvc.perform(post("/api/v1/payment/{orderId}", ORDER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentialsDto)));

        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.FORBIDDEN,
                "msg; code: request-id; request-id: code");

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(print());
    }

    @Test
    public void stripeCheckout_ThrowInvalidOrderException_ReturnsBadRequestAndResponseStatusExceptionDto() throws Exception {
        String invalidOrderExceptionMsg = String.format("The order with id: %s does not exist or has already been paid!",
                ORDER_ID);
        doThrow(new OrderNotFoundException(invalidOrderExceptionMsg))
                .when(paymentService).stripeCheckout(credentialsDto, ORDER_ID);

        ResultActions response = mockMvc.perform(post("/api/v1/payment/{orderId}", ORDER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentialsDto)));

        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.BAD_REQUEST,
                invalidOrderExceptionMsg);

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(print());
    }

    @Test
    public void handlePaymentWebhook_ReturnsNoContentStatus() throws Exception {
        doNothing().when(paymentService).handlePaymentWebhook(any(), any());

        ResultActions response = mockMvc.perform(post("/api/v1/payment/webhook")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(print());
    }

    @Test
    public void handlePaymentWebhook_ThrowIOException_ReturnsBadRequestStatus() throws Exception {
        String iOExceptionMsg = "Error reading from the input stream: Connection reset by peer";

        MockHttpServletRequest request = spy(MockMvcRequestBuilders.post("/api/v1/payment/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .header(STRIPE_HEADER, "yourStripeHeaderValue")
                .buildRequest(Objects.requireNonNull(webApplicationContext.getServletContext())));

        doThrow(new IOException(iOExceptionMsg)).when(request).getReader();

        ResultActions response = mockMvc.perform(sr -> request);

        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.BAD_REQUEST, iOExceptionMsg);

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(print());
    }

    @Test
    public void handlePaymentWebhook_ThrowStripeException_ReturnsForbiddenStatusAndResponseStatusExceptionDto() throws Exception {
        StripeException stripeException = new AuthenticationException("msg", "code", "request-id", 403);

        doThrow(stripeException).when(paymentService).handlePaymentWebhook(any(), any());

        ResultActions response = mockMvc.perform(post("/api/v1/payment/webhook")
                .contentType(MediaType.APPLICATION_JSON));

        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.FORBIDDEN,
                "msg; code: request-id; request-id: code");

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(print());
    }

    @Test
    public void handlePaymentWebhook_ThrowMessageSendingException_ReturnsServiceUnavailableAndResponseStatusExceptionDto() throws Exception {
        String messageSendingExceptionMsg = "Failed to send the email";

        doThrow(new MessageSendingException(messageSendingExceptionMsg))
                .when(paymentService).handlePaymentWebhook(any(), any());

        ResultActions response = mockMvc.perform(post("/api/v1/payment/webhook")
                .contentType(MediaType.APPLICATION_JSON));

        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.SERVICE_UNAVAILABLE,
                messageSendingExceptionMsg);

        response.andExpect(MockMvcResultMatchers.status().isServiceUnavailable())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(print());
    }

    private ExceptionDto getResponseStatusExceptionDto(HttpStatus httpStatus, String msg) {
        return ExceptionDto
                .builder()
                .error(httpStatus.name())
                .status(httpStatus.value())
                .message(msg)
                .build();
    }
}