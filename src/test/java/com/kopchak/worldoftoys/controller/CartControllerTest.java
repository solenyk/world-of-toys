package com.kopchak.worldoftoys.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kopchak.worldoftoys.config.UserDetailsTestConfig;
import com.kopchak.worldoftoys.domain.user.AppUser;
import com.kopchak.worldoftoys.dto.cart.RequestCartItemDto;
import com.kopchak.worldoftoys.dto.cart.UserCartDetailsDto;
import com.kopchak.worldoftoys.dto.error.ResponseStatusExceptionDto;
import com.kopchak.worldoftoys.exception.exception.product.ProductNotFoundException;
import com.kopchak.worldoftoys.service.CartService;
import com.kopchak.worldoftoys.service.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(controllers = CartController.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@Import(UserDetailsTestConfig.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @MockBean
    private JwtTokenService jwtTokenService;

    @Autowired
    private ObjectMapper objectMapper;

    private final static String PRODUCT_SLUG = "product-slug";
    private final static String PRODUCT_NOT_FOUND_EXCEPTION_MSG =
            String.format("Product with slug: %s doesn't exist.", PRODUCT_SLUG);
    private RequestCartItemDto requestCartItemDto;

    @BeforeEach
    public void setUp() {
        requestCartItemDto = new RequestCartItemDto(PRODUCT_SLUG, BigInteger.valueOf(2));
    }


    @Test
    @WithUserDetails(value = "user@example.com", userDetailsServiceBeanName = "userDetailsService")
    public void addProductToCart_RequestCartItemDto_ReturnsCreatedStatus() throws Exception {
        doNothing().when(cartService).addProductToCart(eq(requestCartItemDto), any(AppUser.class));

        ResultActions response = mockMvc.perform(post("/api/v1/cart/add-product")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestCartItemDto))
                .with(csrf()));

        response.andExpect(MockMvcResultMatchers.status().isCreated())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails(value = "user@example.com", userDetailsServiceBeanName = "userDetailsService")
    public void addProductToCart_ThrowProductNotFoundException_ReturnsNotFoundStatusAndResponseStatusExceptionDto() throws Exception {
        doThrow(new ProductNotFoundException(PRODUCT_NOT_FOUND_EXCEPTION_MSG))
                .when(cartService).addProductToCart(eq(requestCartItemDto), any(AppUser.class));

        ResultActions response = mockMvc.perform(post("/api/v1/cart/add-product")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestCartItemDto))
                .with(csrf()));

        response.andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().json(objectMapper.writeValueAsString(getResponseStatusExceptionDto())))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails(value = "user@example.com", userDetailsServiceBeanName = "userDetailsService")
    public void getUserCartDetails_ReturnsOkStatusAndUserCartDetailsDto() throws Exception {
        UserCartDetailsDto expectedUserCartDetailsDto = new UserCartDetailsDto(new HashSet<>(), BigDecimal.ZERO);

        when(cartService.getUserCartDetails(any(AppUser.class))).thenReturn(expectedUserCartDetailsDto);

        ResultActions response = mockMvc.perform(get("/api/v1/cart")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedUserCartDetailsDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails(value = "user@example.com", userDetailsServiceBeanName = "userDetailsService")
    public void updateUserCartItem_RequestCartItemDto_ReturnsNoContentStatus() throws Exception {
        doNothing().when(cartService).updateUserCartItem(eq(requestCartItemDto), any(AppUser.class));

        ResultActions response = mockMvc.perform(patch("/api/v1/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestCartItemDto))
                .with(csrf()));

        response.andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails(value = "user@example.com", userDetailsServiceBeanName = "userDetailsService")
    public void updateUserCartItem_ThrowProductNotFoundException_ReturnsNotFoundStatusAndResponseStatusExceptionDto() throws Exception {
        doThrow(new ProductNotFoundException(PRODUCT_NOT_FOUND_EXCEPTION_MSG))
                .when(cartService).updateUserCartItem(eq(requestCartItemDto), any(AppUser.class));

        ResultActions response = mockMvc.perform(patch("/api/v1/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestCartItemDto))
                .with(csrf()));

        response.andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().json(objectMapper.writeValueAsString(getResponseStatusExceptionDto())))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails(value = "user@example.com", userDetailsServiceBeanName = "userDetailsService")
    public void deleteUserCartItem_RequestCartItemDto_ReturnsNoContentStatus() throws Exception {
        doNothing().when(cartService).deleteUserCartItem(eq(requestCartItemDto), any(AppUser.class));

        ResultActions response = mockMvc.perform(delete("/api/v1/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestCartItemDto))
                .with(csrf()));

        response.andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails(value = "user@example.com", userDetailsServiceBeanName = "userDetailsService")
    public void deleteUserCartItem_ThrowProductNotFoundException_ReturnsNotFoundStatusAndResponseStatusExceptionDto() throws Exception {
        doThrow(new ProductNotFoundException(PRODUCT_NOT_FOUND_EXCEPTION_MSG))
                .when(cartService).deleteUserCartItem(eq(requestCartItemDto), any(AppUser.class));

        ResultActions response = mockMvc.perform(delete("/api/v1/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestCartItemDto))
                .with(csrf()));

        response.andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().json(objectMapper.writeValueAsString(getResponseStatusExceptionDto())))
                .andDo(MockMvcResultHandlers.print());
    }

    private ResponseStatusExceptionDto getResponseStatusExceptionDto() {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        return ResponseStatusExceptionDto
                .builder()
                .error(httpStatus.name())
                .status(httpStatus.value())
                .message(PRODUCT_NOT_FOUND_EXCEPTION_MSG)
                .build();
    }
}