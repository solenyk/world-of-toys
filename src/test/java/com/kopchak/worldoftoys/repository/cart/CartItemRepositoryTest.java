package com.kopchak.worldoftoys.repository.cart;

import com.kopchak.worldoftoys.dto.cart.CartItemDto;
import com.kopchak.worldoftoys.model.cart.CartItem;
import com.kopchak.worldoftoys.model.cart.CartItemId;
import com.kopchak.worldoftoys.model.product.Product;
import com.kopchak.worldoftoys.model.user.AppUser;
import com.kopchak.worldoftoys.repository.product.ProductRepository;
import com.kopchak.worldoftoys.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("integrationtest")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class CartItemRepositoryTest {
    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    public void insertUserCartItem_NonExistentCartItem() {
        String userEmail = "john.doe@example.com";
        String productSlug = "lyalka-rusalochka";
        int quantity = 2;

        cartItemRepository.insertUserCartItem(userEmail, productSlug, quantity);
        Optional<CartItem> returnedCartItem = getOptionalCartItem(userEmail, productSlug);

        assertThat(returnedCartItem).isNotNull();
        assertThat(returnedCartItem).isPresent();
        assertThat(returnedCartItem.get().getQuantity()).isEqualTo(quantity);
    }

    @Test
    public void insertUserCartItem_ExistentCartItem() {
        String userEmail = "john.doe@example.com";
        String productSlug = "lyalka-darynka";
        int quantity = 3;

        cartItemRepository.insertUserCartItem(userEmail, productSlug, quantity);

        Optional<CartItem> returnedCartItem = getOptionalCartItem(userEmail, productSlug);
        int expectedQuantity = 4;

        assertThat(returnedCartItem).isNotNull();
        assertThat(returnedCartItem).isPresent();
        assertThat(returnedCartItem.get().getQuantity()).isEqualTo(expectedQuantity);
    }

    @Test
    public void updateUserCartItem_CartItem() {
        String userEmail = "john.doe@example.com";
        String productSlug = "lyalka-darynka";
        int quantity = 3;

        cartItemRepository.updateUserCartItem(userEmail, productSlug, quantity);
        Optional<CartItem> returnedCartItem = getOptionalCartItem(userEmail, productSlug);

        assertThat(returnedCartItem).isNotNull();
        assertThat(returnedCartItem).isPresent();
        assertThat(returnedCartItem.get().getQuantity()).isEqualTo(quantity);
    }

    @Test
    public void deleteUserCartItem_CartItem() {
        String userEmail = "john.doe@example.com";
        String productSlug = "lyalka-darynka";

        cartItemRepository.deleteUserCartItem(userEmail, productSlug);
        Optional<CartItem> returnedCartItem = getOptionalCartItem(userEmail, productSlug);

        assertThat(returnedCartItem).isNotNull();
        assertThat(returnedCartItem).isEmpty();
    }

    //TODO: дописати для порівняння по set
    @Test
    public void findAllCartItemDtosByUserEmail_ExistentProductSlug_ReturnsOptionalOfProduct() {
        String userEmail = "john.doe@example.com";

        Set<CartItemDto> returnedCartItemDtos = cartItemRepository.findAllCartItemDtosByUserEmail(userEmail);

        assertThat(returnedCartItemDtos).isNotNull();
        assertThat(returnedCartItemDtos).isNotEmpty();
        assertThat(returnedCartItemDtos.size()).isEqualTo(2);
    }

    @Test
    public void calculateUserCartTotalByEmail_ExistentProductSlug_ReturnsOptionalOfProduct() {
        String userEmail = "john.doe@example.com";
        BigDecimal expectedTotalPrice = BigDecimal.valueOf(2900);

        BigDecimal actualTotalPrice = cartItemRepository.calculateUserCartTotalByEmail(userEmail);

        assertThat(actualTotalPrice).isNotNull();
        assertThat(actualTotalPrice).isEqualByComparingTo(expectedTotalPrice);
    }

    Optional<CartItem> getOptionalCartItem(String userEmail, String productSlug){
        AppUser appUser = userRepository.findByEmail(userEmail).orElseThrow(() ->
                new IllegalStateException("User with username: " + userEmail + " doesn't exist"));
        Product product = productRepository.findBySlug(productSlug).orElseThrow(() ->
                new IllegalStateException("Product with slug: " + productSlug + " doesn't exist"));
        CartItemId cartItemId = new CartItemId(appUser, product);
        return cartItemRepository.findById(cartItemId);
    }
}