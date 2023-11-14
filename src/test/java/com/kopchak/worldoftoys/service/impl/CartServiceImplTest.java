package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.cart.RequestCartItemDto;
import com.kopchak.worldoftoys.dto.cart.UserCartDetailsDto;
import com.kopchak.worldoftoys.exception.ProductNotFoundException;
import com.kopchak.worldoftoys.model.cart.CartItem;
import com.kopchak.worldoftoys.model.cart.CartItemId;
import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.model.user.AppUser;
import com.kopchak.worldoftoys.repository.cart.CartItemRepository;
import com.kopchak.worldoftoys.repository.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private CartServiceImpl cartService;
    private final static String PRODUCT_SLUG = "product-slug";

    private RequestCartItemDto requestCartItemDto;
    private AppUser user;
    private Product product;
    private CartItemId cartItemId;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        requestCartItemDto = new RequestCartItemDto(PRODUCT_SLUG, 2);
        user = new AppUser();
        product = new Product();
        cartItemId = new CartItemId(user, product);
        cartItem = new CartItem(cartItemId, 3);
    }

    @Test
    public void addProductToCart_ExistentCartItem() {
        int expectedCartItemQuantity = requestCartItemDto.quantity() + cartItem.getQuantity();

        when(productRepository.findBySlug(PRODUCT_SLUG)).thenReturn(Optional.of(product));
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));

        cartService.addProductToCart(requestCartItemDto, user);

        assertThat(cartItem.getQuantity()).isEqualTo(expectedCartItemQuantity);
        verify(cartItemRepository).save(cartItem);
    }

    @Test
    public void addProductToCart_NonExistentCartItem() {
        when(productRepository.findBySlug(PRODUCT_SLUG)).thenReturn(Optional.of(product));
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.empty());

        cartService.addProductToCart(requestCartItemDto, user);

        ArgumentCaptor<CartItem> cartItemArgumentCaptor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(cartItemArgumentCaptor.capture());
        CartItem cartItem = cartItemArgumentCaptor.getValue();
        assertThat(cartItem.getQuantity()).isEqualTo(requestCartItemDto.quantity());
    }

    @Test
    public void addProductToCart_NonExistentProductSlug() {
        assertResponseStatusException(() -> cartService.addProductToCart(requestCartItemDto, user));
    }

    @Test
    public void getUserCartDetails_ExistentUserEmail_ReturnsUserCartDetailsDto() {
        BigDecimal expectedTotalCost = BigDecimal.ZERO;

        when(cartItemRepository.findAllUserCartItems(user)).thenReturn(new HashSet<>());
        when(cartItemRepository.calculateUserCartTotalPrice(user)).thenReturn(expectedTotalCost);

        UserCartDetailsDto userCartDetailsDto = cartService.getUserCartDetails(user);

        assertThat(userCartDetailsDto.content()).isNotNull();
        assertThat(userCartDetailsDto.content()).isEmpty();
        assertThat(userCartDetailsDto.totalCost()).isEqualByComparingTo(expectedTotalCost);
    }

    @Test
    public void updateUserCartItem_ExistentUserEmailAndProductSlug() {
        when(productRepository.findBySlug(PRODUCT_SLUG)).thenReturn(Optional.of(product));

        cartService.updateUserCartItem(requestCartItemDto, user);

        ArgumentCaptor<CartItem> cartItemArgumentCaptor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(cartItemArgumentCaptor.capture());
        CartItem cartItem = cartItemArgumentCaptor.getValue();
        assertThat(cartItem.getQuantity()).isEqualTo(requestCartItemDto.quantity());
    }

    @Test
    public void updateUserCartItem_NonExistentProductSlug() {
        assertResponseStatusException(() -> cartService.updateUserCartItem(requestCartItemDto, user));
    }

    @Test
    public void deleteUserCartItem_ExistentUserEmailAndProductSlug() {
        when(productRepository.findBySlug(PRODUCT_SLUG)).thenReturn(Optional.of(product));
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));

        cartService.deleteUserCartItem(requestCartItemDto, user);

        ArgumentCaptor<CartItem> cartItemArgumentCaptor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).delete(cartItemArgumentCaptor.capture());
        CartItem capturedCartItem = cartItemArgumentCaptor.getValue();
        assertThat(capturedCartItem.getId()).isEqualTo(cartItem.getId());
        assertThat(capturedCartItem.getQuantity()).isEqualTo(cartItem.getQuantity());
    }

    @Test
    public void deleteUserCartItem_NonExistentProductSlug() {
        assertResponseStatusException(() -> cartService.deleteUserCartItem(requestCartItemDto, user));
    }

    private void assertResponseStatusException(Executable executable) {
        ResponseStatusException exception = assertThrows(ProductNotFoundException.class, executable);

        String actualMessage = exception.getReason();
        int expectedStatusCode = HttpStatus.NOT_FOUND.value();
        int actualStatusCode = exception.getStatusCode().value();

        assertEquals("Product doesn't exist", actualMessage);
        assertEquals(expectedStatusCode, actualStatusCode);
    }
}