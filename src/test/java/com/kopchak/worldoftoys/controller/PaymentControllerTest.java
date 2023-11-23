package com.kopchak.worldoftoys.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kopchak.worldoftoys.config.UserDetailsTestConfig;
import com.kopchak.worldoftoys.dto.error.ResponseStatusExceptionDto;
import com.kopchak.worldoftoys.dto.payment.StripeCredentialsDto;
import com.kopchak.worldoftoys.service.JwtTokenService;
import com.kopchak.worldoftoys.service.PaymentService;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@WebMvcTest(controllers = PaymentController.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@Import(UserDetailsTestConfig.class)
class PaymentControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private PaymentService paymentService;

    @MockBean
    private JwtTokenService jwtTokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithUserDetails(value = "user@example.com", userDetailsServiceBeanName = "userDetailsService")
    public void stripeCheckout_AuthUser_ReturnsFoundStatusAndRedirectsToStripeCheckoutPage() throws Exception {
        String orderId = "order_id";
        var credentialsDto = new StripeCredentialsDto("user", "user@test.com");
        String expectedStripeCheckoutUserUrl = "stripe_checkout_url";

        when(paymentService.isNonExistentOrPaidOrder(orderId)).thenReturn(false);
        when(paymentService.stripeCheckout(credentialsDto, orderId)).thenReturn(expectedStripeCheckoutUserUrl);

        ResultActions response = mockMvc.perform(post("/api/v1/payment/{orderId}", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentialsDto))
                .with(csrf()));

        response.andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(redirectedUrl(expectedStripeCheckoutUserUrl))
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "user@example.com", userDetailsServiceBeanName = "userDetailsService")
    public void stripeCheckout_ThrowStripeException_ReturnsForbiddenStatusAndResponseStatusExceptionDto() throws Exception {
        String orderId = "order_id";
        var credentialsDto = new StripeCredentialsDto("user", "user@test.com");
        StripeException stripeException = new AuthenticationException("msg", "code", "request-id", 403);

        when(paymentService.isNonExistentOrPaidOrder(orderId)).thenReturn(false);
        when(paymentService.stripeCheckout(credentialsDto, orderId)).thenThrow(stripeException);

        ResultActions response = mockMvc.perform(post("/api/v1/payment/{orderId}", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentialsDto))
                .with(csrf()));

        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.FORBIDDEN,
                "msg; code: request-id; request-id: code");

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "user@example.com", userDetailsServiceBeanName = "userDetailsService")
    public void stripeCheckout_AuthUserNonExistentOrder_ReturnsBadRequestStatusAndResponseStatusExceptionDto() throws Exception {
        String orderId = "order_id";
        var credentialsDto = new StripeCredentialsDto("user", "user@test.com");

        when(paymentService.isNonExistentOrPaidOrder(orderId)).thenReturn(true);

        ResultActions response = mockMvc.perform(post("/api/v1/payment/{orderId}", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentialsDto))
                .with(csrf()));

        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.BAD_REQUEST,
                "The order does not exist or has already been paid!");

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(print());
    }

    @Test
    @WithAnonymousUser
    public void stripeCheckout_AnonymousUser_ReturnsUnauthorizedStatus() throws Exception {
        String orderId = "order_id";
        var credentialsDto = new StripeCredentialsDto("user", "user@test.com");
        String expectedStripeCheckoutUserUrl = "stripe_checkout_url";

        when(paymentService.isNonExistentOrPaidOrder(orderId)).thenReturn(false);
        when(paymentService.stripeCheckout(credentialsDto, orderId)).thenReturn(expectedStripeCheckoutUserUrl);

        ResultActions response = mockMvc.perform(post("/api/v1/payment/{orderId}", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentialsDto))
                .with(csrf()));

        response.andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "user@example.com", userDetailsServiceBeanName = "userDetailsService")
    public void handlePaymentWebhook_ReturnsNoContentStatus() throws Exception {
        doNothing().when(paymentService).handlePaymentWebhook(any(), any());

        ResultActions response = mockMvc.perform(post("/api/v1/payment/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()));

        response.andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "user@example.com", userDetailsServiceBeanName = "userDetailsService")
    public void handlePaymentWebhook_ThrowStripeException_ReturnsBadRequestStatusAndResponseStatusExceptionDto() throws Exception {
        StripeException stripeException = new SignatureVerificationException("msg", "signHeader");
        doThrow(stripeException).when(paymentService).handlePaymentWebhook(any(), any());

        ResultActions response = mockMvc.perform(post("/api/v1/payment/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()));

        var responseStatusExceptionDto = getResponseStatusExceptionDto(HttpStatus.BAD_REQUEST, "msg");

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(print());
    }

    private ResponseStatusExceptionDto getResponseStatusExceptionDto(HttpStatus httpStatus, String msg) {
        return ResponseStatusExceptionDto
                .builder()
                .error(httpStatus.name())
                .status(httpStatus.value())
                .message(msg)
                .build();
    }
}