package com.kopchak.worldoftoys.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kopchak.worldoftoys.config.UserDetailsTestConfig;
import com.kopchak.worldoftoys.dto.cart.RequestCartItemDto;
import com.kopchak.worldoftoys.dto.cart.UserCartDetailsDto;
import com.kopchak.worldoftoys.model.user.AppUser;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.util.HashSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
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

    private RequestCartItemDto requestCartItemDto;

    @BeforeEach
    public void setUp() {
        requestCartItemDto = new RequestCartItemDto("product-slug", 2);
    }


    @Test
    @WithUserDetails(value = "user@example.com", userDetailsServiceBeanName = "userDetailsService")
    public void addProductToCart_AuthUserAndRequestCartItemDto_ReturnsCreatedStatus() throws Exception {
        doNothing().when(cartService).addProductToCart(eq(requestCartItemDto), any(AppUser.class));

        ResultActions response = mockMvc.perform(post("/api/v1/cart/add-product")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestCartItemDto))
                .with(csrf()));

        response.andExpect(MockMvcResultMatchers.status().isCreated())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    public void addProductToCart_AnonymousUserAndRequestCartItemDto_ReturnsUnauthorizedStatus() throws Exception {
        ResultActions response = mockMvc.perform(post("/api/v1/cart/add-product")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestCartItemDto))
                .with(csrf()));

        response.andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails(value = "user@example.com", userDetailsServiceBeanName = "userDetailsService")
    public void getUserCartDetails_AuthUser_ReturnsOkStatusAndUserCartDetailsDto() throws Exception {
        UserCartDetailsDto expectedUserCartDetailsDto = new UserCartDetailsDto(new HashSet<>(), BigDecimal.ZERO);

        when(cartService.getUserCartDetails(any(AppUser.class))).thenReturn(expectedUserCartDetailsDto);

        ResultActions response = mockMvc.perform(get("/api/v1/cart")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedUserCartDetailsDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    public void getUserCartDetails_AnonymousUser_ReturnsUnauthorizedStatus() throws Exception {
        ResultActions response = mockMvc.perform(get("/api/v1/cart")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails(value = "user@example.com", userDetailsServiceBeanName = "userDetailsService")
    public void updateUserCartItem_AuthUserAndRequestCartItemDto_ReturnsNoContentStatus() throws Exception {
        doNothing().when(cartService).updateUserCartItem(eq(requestCartItemDto), any(AppUser.class));

        ResultActions response = mockMvc.perform(patch("/api/v1/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestCartItemDto))
                .with(csrf()));

        response.andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    public void updateUserCartItem_AnonymousUserAndRequestCartItemDto_ReturnsUnauthorizedStatus() throws Exception {
        ResultActions response = mockMvc.perform(patch("/api/v1/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestCartItemDto))
                .with(csrf()));

        response.andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails(value = "user@example.com", userDetailsServiceBeanName = "userDetailsService")
    public void deleteUserCartItem_AuthUserAndRequestCartItemDto_ReturnsNoContentStatus() throws Exception {
        doNothing().when(cartService).deleteUserCartItem(eq(requestCartItemDto), any(AppUser.class));

        ResultActions response = mockMvc.perform(delete("/api/v1/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestCartItemDto))
                .with(csrf()));

        response.andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    public void deleteUserCartItem_AnonymousUserAndRequestCartItemDto_ReturnsUnauthorizedStatus() throws Exception {
        ResultActions response = mockMvc.perform(delete("/api/v1/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestCartItemDto))
                .with(csrf()));

        response.andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andDo(MockMvcResultHandlers.print());
    }

}