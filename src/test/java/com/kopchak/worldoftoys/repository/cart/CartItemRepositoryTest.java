package com.kopchak.worldoftoys.repository.cart;

import com.kopchak.worldoftoys.domain.cart.CartItem;
import com.kopchak.worldoftoys.domain.cart.CartItemId;
import com.kopchak.worldoftoys.domain.product.Product;
import com.kopchak.worldoftoys.dto.cart.CartItemDto;
import com.kopchak.worldoftoys.domain.user.AppUser;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("integrationtest")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class CartItemRepositoryTest {
    @Autowired
    CartItemRepository cartItemRepository;
    private AppUser user;

    @BeforeEach
    public void setUp() {
        user = AppUser.builder().id(1000).build();
    }

    @Test
    public void calculateUserCartTotalPrice_UserEmail_ReturnsBigDecimal() {
        BigDecimal expectedTotalPrice = BigDecimal.valueOf(2900);

        BigDecimal actualTotalPrice = cartItemRepository.calculateUserCartTotalPrice(user);

        assertThat(actualTotalPrice).isNotNull();
        assertThat(actualTotalPrice).isEqualByComparingTo(expectedTotalPrice);
    }

    @Test
    public void deleteAllById_User_ReturnsCartItemSet() {
        Set<CartItem> cartItems = cartItemRepository.deleteAllById_User(user);
        Set<CartItemDto> cartItemDtos = cartItemRepository.findAllUserCartItems(user);

        assertThat(cartItems).isNotNull();
        assertThat(cartItems).isNotEmpty();
        assertThat(cartItems.size()).isEqualTo(2);
        assertThat(cartItemDtos).isNotNull();
        assertThat(cartItemDtos).isEmpty();
    }

    @Test
    public void findAllUserCartItems_UserEmail_ReturnsSetOfCartItemDto() {
        int expectedProductAmount = 2;
        List<CartItemDto> expectedCartItemDtos = new ArrayList<>() {{
            add(new CartItemDto("Лялька Даринка", "lyalka-darynka", BigDecimal.valueOf(900), BigInteger.ONE));
            add(new CartItemDto("Пупсик Оксанка", "pupsik_oksanka", BigDecimal.valueOf(2000),
                    BigInteger.valueOf(4)));
        }};

        List<CartItemDto> actualCartItemDtos = cartItemRepository.findAllUserCartItems(user)
                .stream().toList();

        assertThat(actualCartItemDtos).isNotNull();
        assertThat(actualCartItemDtos).isNotEmpty();
        assertThat(actualCartItemDtos.size()).isEqualTo(expectedProductAmount);

        for (int i = 0; i < expectedProductAmount; i++) {
            assertThat(actualCartItemDtos.get(i).name()).isEqualTo(expectedCartItemDtos.get(i).name());
            assertThat(actualCartItemDtos.get(i).slug()).isEqualTo(expectedCartItemDtos.get(i).slug());
            assertThat(actualCartItemDtos.get(i).totalProductPrice()).isEqualByComparingTo(
                    expectedCartItemDtos.get(i).totalProductPrice());
            assertThat(actualCartItemDtos.get(i).quantity()).isEqualTo(expectedCartItemDtos.get(i).quantity());
        }
    }
}