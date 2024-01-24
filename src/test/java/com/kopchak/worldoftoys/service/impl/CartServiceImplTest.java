package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.domain.cart.CartItem;
import com.kopchak.worldoftoys.domain.cart.CartItemId;
import com.kopchak.worldoftoys.domain.product.Product;
import com.kopchak.worldoftoys.domain.user.AppUser;
import com.kopchak.worldoftoys.dto.cart.RequestCartItemDto;
import com.kopchak.worldoftoys.dto.cart.UserCartDetailsDto;
import com.kopchak.worldoftoys.exception.exception.cart.CartValidationException;
import com.kopchak.worldoftoys.exception.exception.product.ProductNotFoundException;
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

import java.math.BigDecimal;
import java.math.BigInteger;
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
    private final static String PRODUCT_NOT_FOUND_MSG =
            String.format("Product with slug: %s doesn't exist.", PRODUCT_SLUG);

    private RequestCartItemDto requestCartItemDto;
    private AppUser user;
    private Product product;
    private CartItemId cartItemId;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        requestCartItemDto = new RequestCartItemDto(PRODUCT_SLUG, BigInteger.valueOf(2));
        user = new AppUser();
        product = new Product();
        cartItemId = new CartItemId(user, product);
        cartItem = new CartItem(cartItemId, BigInteger.valueOf(3));
    }

    @Test
    public void addProductToCart_ExistentCartItem() throws ProductNotFoundException {
        BigInteger expectedCartItemQuantity = requestCartItemDto.quantity().add(cartItem.getQuantity());

        when(productRepository.findBySlug(PRODUCT_SLUG)).thenReturn(Optional.of(product));
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));

        cartService.addProductToCart(requestCartItemDto, user);

        assertThat(cartItem.getQuantity()).isEqualTo(expectedCartItemQuantity);
        verify(cartItemRepository).save(cartItem);
    }

    @Test
    public void addProductToCart_NonExistentCartItem() throws ProductNotFoundException {
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
        assertException(ProductNotFoundException.class, PRODUCT_NOT_FOUND_MSG,
                () -> cartService.addProductToCart(requestCartItemDto, user));
    }

    @Test
    public void verifyCartBeforeOrderCreation_InvalidCartItems_ThrowsCartValidationException() {
        String cartValidationExceptionMsg = "Some products in the cart are not available in the selected quantity " +
                "because one or more products are out of stock";

        when(cartItemRepository.deleteUnavailableItems(user)).thenReturn(1);
        when(cartItemRepository.updateCartItems(user)).thenReturn(0);

        assertException(CartValidationException.class, cartValidationExceptionMsg,
                () -> cartService.verifyCartBeforeOrderCreation(user));
    }

    @Test
    public void verifyCartBeforeOrderCreation_ValidCartItems() throws CartValidationException {
        when(cartItemRepository.deleteUnavailableItems(user)).thenReturn(0);
        when(cartItemRepository.updateCartItems(user)).thenReturn(0);

        cartService.verifyCartBeforeOrderCreation(user);

        verify(cartItemRepository).deleteUnavailableItems(user);
        verify(cartItemRepository).updateCartItems(user);
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
    public void updateUserCartItem_ExistentUserEmailAndProductSlug() throws ProductNotFoundException {
        when(productRepository.findBySlug(PRODUCT_SLUG)).thenReturn(Optional.of(product));

        cartService.updateUserCartItem(requestCartItemDto, user);

        ArgumentCaptor<CartItem> cartItemArgumentCaptor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(cartItemArgumentCaptor.capture());
        CartItem cartItem = cartItemArgumentCaptor.getValue();
        assertThat(cartItem.getQuantity()).isEqualTo(requestCartItemDto.quantity());
    }

    @Test
    public void updateUserCartItem_NonExistentProductSlug() {
        assertException(ProductNotFoundException.class, PRODUCT_NOT_FOUND_MSG,
                () -> cartService.updateUserCartItem(requestCartItemDto, user));
    }

    @Test
    public void deleteUserCartItem_ExistentUserEmailAndProductSlug() throws ProductNotFoundException {
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
        assertException(ProductNotFoundException.class, PRODUCT_NOT_FOUND_MSG,
                () -> cartService.deleteUserCartItem(requestCartItemDto, user));
    }

    private void assertException(Class<? extends Exception> expectedExceptionType, String expectedMessage,
                                 Executable executable) {
        Exception exception = assertThrows(expectedExceptionType, executable);
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }
}