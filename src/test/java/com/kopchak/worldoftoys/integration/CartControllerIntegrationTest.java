package com.kopchak.worldoftoys.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kopchak.worldoftoys.dto.cart.CartItemDto;
import com.kopchak.worldoftoys.dto.cart.RequestCartItemDto;
import com.kopchak.worldoftoys.dto.cart.UserCartDetailsDto;
import com.kopchak.worldoftoys.dto.error.ResponseStatusExceptionDto;
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
import java.util.LinkedHashSet;
import java.util.Set;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("integrationtest")
@Transactional
public class CartControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;
    private final static String NON_EXISTENT_PRODUCT_SLUG = "non-existent-product-slug";
    private RequestCartItemDto validRequestCartItemDto;
    private RequestCartItemDto invalidRequestCartItemDto;
    private ResponseStatusExceptionDto accessDeniedErrorResponse;
    private ResponseStatusExceptionDto unauthorizedErrorResponse;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        validRequestCartItemDto = new RequestCartItemDto("lyalka-klaymber", 2);
        invalidRequestCartItemDto = new RequestCartItemDto(NON_EXISTENT_PRODUCT_SLUG, 2);
        accessDeniedErrorResponse = new ResponseStatusExceptionDto(HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.name(), "Access Denied");
        unauthorizedErrorResponse = new ResponseStatusExceptionDto(HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.name(), "Full authentication is required to access this resource");
    }

    @Test
    @WithUserDetails("john.doe@example.com")
    public void addProductToCart_AuthUserAndRequestCartItemDto_ReturnsCreatedStatus() throws Exception {
        ResultActions response = mockMvc.perform(post("/api/v1/cart/add-product")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestCartItemDto)));

        response.andExpect(MockMvcResultMatchers.status().isCreated())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("john.doe@example.com")
    public void addProductToCart_ThrowProductNotFoundException_ReturnsNotFoundStatusAndResponseStatusExceptionDto() throws Exception {
        ResultActions response = mockMvc.perform(post("/api/v1/cart/add-product")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequestCartItemDto)));

        var responseStatusExceptionDto = getProductNotFoundResponseStatusExDto();

        response.andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());
    }


    @Test
    @WithAnonymousUser
    public void addProductToCart_AnonymousUserAndRequestCartItemDto_ReturnsForbiddenStatus() throws Exception {
        ResultActions response = mockMvc.perform(post("/api/v1/cart/add-product")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestCartItemDto)));

        response.andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().json(objectMapper.writeValueAsString(unauthorizedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("jane.smith@example.com")
    public void addProductToCart_AuthAdminAndRequestCartItemDto_ReturnsForbiddenStatus() throws Exception {
        ResultActions response = mockMvc.perform(post("/api/v1/cart/add-product")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestCartItemDto)));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().json(objectMapper.writeValueAsString(accessDeniedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("john.doe@example.com")
    public void getUserCartDetails_AuthUser_ReturnsOkStatusAndUserCartDetailsDto() throws Exception {
        Set<CartItemDto> expectedContent = new LinkedHashSet<>() {{
            add(new CartItemDto("Лялька Даринка", "lyalka-darynka", BigDecimal.valueOf(900), 1));
            add(new CartItemDto("Пупсик Оксанка", "pupsik_oksanka", BigDecimal.valueOf(2000), 4));
        }};
        BigDecimal expectedTotalCost = BigDecimal.valueOf(2900);
        UserCartDetailsDto expectedCartDetailsDto = new UserCartDetailsDto(expectedContent, expectedTotalCost);

        ResultActions response = mockMvc.perform(get("/api/v1/cart")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedCartDetailsDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    public void getUserCartDetails_AnonymousUser_ReturnsForbiddenStatus() throws Exception {
        ResultActions response = mockMvc.perform(get("/api/v1/cart")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().json(objectMapper.writeValueAsString(unauthorizedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("jane.smith@example.com")
    public void getUserCartDetails_AuthAdmin_ReturnsForbiddenStatus() throws Exception {
        ResultActions response = mockMvc.perform(get("/api/v1/cart")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().json(objectMapper.writeValueAsString(accessDeniedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("john.doe@example.com")
    public void updateUserCartItem_AuthUserAndRequestCartItemDto_ReturnsNoContentStatus() throws Exception {
        ResultActions response = mockMvc.perform(patch("/api/v1/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestCartItemDto)));

        response.andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("john.doe@example.com")
    public void updateUserCartItem_ThrowProductNotFoundException_ReturnsNotFoundStatusAndResponseStatusExceptionDto() throws Exception {
        ResultActions response = mockMvc.perform(patch("/api/v1/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequestCartItemDto)));

        var responseStatusExceptionDto = getProductNotFoundResponseStatusExDto();

        response.andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    public void updateUserCartItem_AnonymousUser_ReturnsForbiddenStatus() throws Exception {
        ResultActions response = mockMvc.perform(patch("/api/v1/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestCartItemDto)));

        response.andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().json(objectMapper.writeValueAsString(unauthorizedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("jane.smith@example.com")
    public void updateUserCartItem_AuthAdmin_ReturnsForbiddenStatus() throws Exception {
        ResultActions response = mockMvc.perform(patch("/api/v1/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestCartItemDto)));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().json(objectMapper.writeValueAsString(accessDeniedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("john.doe@example.com")
    public void deleteUserCartItem_AuthUserAndRequestCartItemDto_ReturnsNoContentStatus() throws Exception {
        ResultActions response = mockMvc.perform(delete("/api/v1/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestCartItemDto)));

        response.andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("john.doe@example.com")
    public void deleteUserCartItem_ThrowProductNotFoundException_ReturnsNotFoundStatusAndResponseStatusExceptionDto() throws Exception {
        ResultActions response = mockMvc.perform(delete("/api/v1/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequestCartItemDto)));

        var responseStatusExceptionDto = getProductNotFoundResponseStatusExDto();

        response.andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(content().json(objectMapper.writeValueAsString(responseStatusExceptionDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithAnonymousUser
    public void deleteUserCartItem_AnonymousUser_ReturnsForbiddenStatus() throws Exception {
        ResultActions response = mockMvc.perform(delete("/api/v1/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestCartItemDto)));

        response.andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(content().json(objectMapper.writeValueAsString(unauthorizedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithUserDetails("jane.smith@example.com")
    public void deleteUserCartItem_AuthAdmin_ReturnsForbiddenStatus() throws Exception {
        ResultActions response = mockMvc.perform(delete("/api/v1/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestCartItemDto)));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(content().json(objectMapper.writeValueAsString(accessDeniedErrorResponse)))
                .andDo(MockMvcResultHandlers.print());
    }

    private ResponseStatusExceptionDto getProductNotFoundResponseStatusExDto() {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        String msg = String.format("Product with slug: %s doesn't exist.", NON_EXISTENT_PRODUCT_SLUG);
        return ResponseStatusExceptionDto
                .builder()
                .error(httpStatus.name())
                .status(httpStatus.value())
                .message(msg)
                .build();
    }
}
