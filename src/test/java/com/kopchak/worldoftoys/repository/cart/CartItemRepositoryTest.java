package com.kopchak.worldoftoys.repository.cart;

import com.kopchak.worldoftoys.dto.cart.CartItemDto;
import com.kopchak.worldoftoys.repository.product.ProductRepository;
import com.kopchak.worldoftoys.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
    private final static String USER_EMAIL = "john.doe@example.com";

    @Test
    public void calculateUserCartTotalByEmail_ExistentProductSlug_ReturnsOptionalOfProduct() {
        BigDecimal expectedTotalPrice = BigDecimal.valueOf(2900);

        BigDecimal actualTotalPrice = cartItemRepository.calculateUserCartTotalByEmail(USER_EMAIL);

        assertThat(actualTotalPrice).isNotNull();
        assertThat(actualTotalPrice).isEqualByComparingTo(expectedTotalPrice);
    }

    @Test
    public void findAllCartItemDtosByUserEmail_ExistentProductSlug_ReturnsOptionalOfProduct() {
        int expectedProductAmount = 2;
        List<CartItemDto> expectedCartItemDtos = new ArrayList<>() {{
            add(new CartItemDto("Лялька Даринка", "lyalka-darynka", BigDecimal.valueOf(900), 1));
            add(new CartItemDto("Пупсик Оксанка", "pupsik_oksanka", BigDecimal.valueOf(2000), 4));
        }};

        List<CartItemDto> actualCartItemDtos = cartItemRepository.findAllCartItemDtosByUserEmail(USER_EMAIL)
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