package com.kopchak.worldoftoys.service.impl;

import com.kopchak.worldoftoys.dto.cart.RequestCartItemDto;
import com.kopchak.worldoftoys.dto.cart.UserCartDetailsDto;
import com.kopchak.worldoftoys.exception.ProductNotFoundException;
import com.kopchak.worldoftoys.exception.UserNotFoundException;
import com.kopchak.worldoftoys.model.cart.CartItem;
import com.kopchak.worldoftoys.model.cart.CartItemId;
import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.model.user.AppUser;
import com.kopchak.worldoftoys.repository.cart.CartItemRepository;
import com.kopchak.worldoftoys.repository.product.ProductRepository;
import com.kopchak.worldoftoys.repository.user.UserRepository;
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
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private CartServiceImpl cartService;

    private final static String USER_EMAIL = "user@example.com";
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
        cartItem = new CartItem(cartItemId,3);
    }

    @Test
    public void addProductToCart_ExistentCartItem() {
        int expectedCartItemQuantity = requestCartItemDto.quantity() + cartItem.getQuantity();

        mockMethodsCallForUserAndProductRepositories();
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));

        cartService.addProductToCart(requestCartItemDto, USER_EMAIL);

        assertThat(cartItem.getQuantity()).isEqualTo(expectedCartItemQuantity);
        verify(cartItemRepository).save(cartItem);
    }

    @Test
    public void addProductToCart_NonExistentCartItem() {
        mockMethodsCallForUserAndProductRepositories();
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.empty());

        cartService.addProductToCart(requestCartItemDto, USER_EMAIL);

        ArgumentCaptor<CartItem> cartItemArgumentCaptor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(cartItemArgumentCaptor.capture());
        CartItem cartItem = cartItemArgumentCaptor.getValue();
        assertThat(cartItem.getQuantity()).isEqualTo(requestCartItemDto.quantity());
    }

    @Test
    public void getUserCartDetails_ExistentUserEmail_ReturnsUserCartDetailsDto() {
        BigDecimal expectedTotalCost = BigDecimal.ZERO;

        when(cartItemRepository.findAllCartItemDtosByUserEmail(USER_EMAIL)).thenReturn(new HashSet<>());
        when(cartItemRepository.calculateUserCartTotalByEmail(USER_EMAIL)).thenReturn(expectedTotalCost);

        UserCartDetailsDto userCartDetailsDto = cartService.getUserCartDetails(USER_EMAIL);

        assertThat(userCartDetailsDto.content()).isNotNull();
        assertThat(userCartDetailsDto.content()).isEmpty();
        assertThat(userCartDetailsDto.totalCost()).isEqualByComparingTo(expectedTotalCost);
    }

    @Test
    public void updateUserCartItem_ExistentUserEmailAndProductSlug() {
        mockMethodsCallForUserAndProductRepositories();

        cartService.updateUserCartItem(requestCartItemDto, USER_EMAIL);

        ArgumentCaptor<CartItem> cartItemArgumentCaptor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(cartItemArgumentCaptor.capture());
        CartItem cartItem = cartItemArgumentCaptor.getValue();
        assertThat(cartItem.getQuantity()).isEqualTo(requestCartItemDto.quantity());
    }

    @Test
    public void deleteUserCartItem_ExistentUserEmailAndProductSlug() {
        mockMethodsCallForUserAndProductRepositories();
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));

        cartService.deleteUserCartItem(requestCartItemDto, USER_EMAIL);

        ArgumentCaptor<CartItem> cartItemArgumentCaptor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).delete(cartItemArgumentCaptor.capture());
        CartItem capturedCartItem = cartItemArgumentCaptor.getValue();
        assertThat(capturedCartItem.getId()).isEqualTo(cartItem.getId());
        assertThat(capturedCartItem.getQuantity()).isEqualTo(cartItem.getQuantity());
    }

    @Test
    public void deleteUserCartItem_NonExistentUserEmail() {
        assertResponseStatusException(UserNotFoundException.class, "User doesn't exist!",
                () -> cartService.deleteUserCartItem(requestCartItemDto, USER_EMAIL));
    }

    @Test
    public void deleteUserCartItem_ExistentUserEmailAndNonExistentProductSlug() {
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        assertResponseStatusException(ProductNotFoundException.class, "Product doesn't exist",
                () -> cartService.deleteUserCartItem(requestCartItemDto, USER_EMAIL));
    }

    private void mockMethodsCallForUserAndProductRepositories(){
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(productRepository.findBySlug(PRODUCT_SLUG)).thenReturn(Optional.of(product));
    }

    private void assertResponseStatusException(Class<? extends ResponseStatusException> expectedExceptionType,
                                               String expectedMessage, Executable executable) {
        ResponseStatusException exception = assertThrows(expectedExceptionType, executable);

        String actualMessage = exception.getReason();
        int expectedStatusCode = HttpStatus.NOT_FOUND.value();
        int actualStatusCode = exception.getStatusCode().value();

        assertEquals(expectedMessage, actualMessage);
        assertEquals(expectedStatusCode, actualStatusCode);
    }
}