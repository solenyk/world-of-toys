package com.kopchak.worldoftoys.repository.order;

import com.kopchak.worldoftoys.model.order.Order;
import com.kopchak.worldoftoys.model.user.AppUser;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("integrationtest")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class OrderRepositoryTest {
    @Autowired
    private OrderRepository orderRepository;
    private AppUser user;
    private String existentOrderId;

    @BeforeEach
    public void setUp() {
        user = AppUser.builder().id(1000).build();
        existentOrderId = "4c980930-16eb-41cd-b998-29d03118d67c";
    }

    @Test
    public void findAllByUser_UserWithOrders_ReturnsOrderSet() {
        Integer expectedOrdersSetSize = 1;

        Set<Order> actualOrdersSet = orderRepository.findAllByUser(user);

        assertThat(actualOrdersSet).isNotNull();
        assertThat(actualOrdersSet).isNotEmpty();
        assertThat(actualOrdersSet.size()).isEqualTo(expectedOrdersSetSize);
        assertThat(actualOrdersSet.stream().findFirst()).isNotEmpty();
        assertThat(actualOrdersSet.stream().findFirst().get().getId()).isEqualTo(existentOrderId);
    }

    @Test
    public void findAllByUser_UserWithoutOrders_ReturnsOrderSet() {
        user.setId(1002);

        Set<Order> actualOrdersSet = orderRepository.findAllByUser(user);

        assertThat(actualOrdersSet).isNotNull();
        assertThat(actualOrdersSet).isEmpty();
    }

    @Test
    public void findById_ExistentOrderId_ReturnsOptionalOfOrder() {
        Optional<Order> actualOrder = orderRepository.findById(existentOrderId);

        assertThat(actualOrder).isPresent();
        assertThat(actualOrder.get().getId()).isEqualTo(existentOrderId);
    }

    @Test
    public void findById_NonExistentOrderId_ReturnsEmptyOptional() {
        String nonExistentOrderId = "4c980930-16eb-41cd-b998-29d03118d67r";

        Optional<Order> actualOrder = orderRepository.findById(nonExistentOrderId);

        assertThat(actualOrder).isEmpty();
    }
}