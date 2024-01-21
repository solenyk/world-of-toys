package com.kopchak.worldoftoys.repository.order;

import com.kopchak.worldoftoys.domain.order.Order;
import com.kopchak.worldoftoys.domain.order.OrderStatus;
import com.kopchak.worldoftoys.domain.order.payment.PaymentStatus;
import com.kopchak.worldoftoys.domain.user.AppUser;
import com.kopchak.worldoftoys.repository.specifications.OrderSpecifications;
import com.kopchak.worldoftoys.repository.specifications.impl.OrderSpecificationsImpl;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("integrationtest")
@Import(OrderSpecificationsImpl.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class OrderRepositoryTest {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    OrderSpecifications orderSpecifications;
    private AppUser user;
    private static final String EXISTENT_ORDER_ID = "4c980930-16eb-41cd-b998-29d03118d67c";

    @BeforeEach
    public void setUp() {
        user = AppUser.builder().id(1000).build();
    }

    @Test
    public void findAllByUser_UserWithOrders_ReturnsOrderSet() {
        Integer expectedOrdersSetSize = 1;

        Set<Order> actualOrdersSet = orderRepository.findAllByUser(user);

        assertThat(actualOrdersSet).isNotNull();
        assertThat(actualOrdersSet).isNotEmpty();
        assertThat(actualOrdersSet.size()).isEqualTo(expectedOrdersSetSize);
        assertThat(actualOrdersSet.stream().findFirst()).isNotEmpty();
        assertThat(actualOrdersSet.stream().findFirst().get().getId()).isEqualTo(EXISTENT_ORDER_ID);
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
        Optional<Order> actualOrder = orderRepository.findById(EXISTENT_ORDER_ID);

        assertThat(actualOrder).isPresent();
        assertThat(actualOrder.get().getId()).isEqualTo(EXISTENT_ORDER_ID);
    }

    @Test
    public void findById_NonExistentOrderId_ReturnsEmptyOptional() {
        String nonExistentOrderId = "4c980930-16eb-41cd-b998-29d03118d67r";

        Optional<Order> actualOrder = orderRepository.findById(nonExistentOrderId);

        assertThat(actualOrder).isEmpty();
    }

    @Test
    public void findAll_ReturnsPageOfOrder() {
        int pageNumber = 0;
        int pageSize = 2;
        int expectedAmountOfPages = 1;
        int expectedAmountOfElements = 1;
        int expectedContentSize = 1;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Specification<Order> spec = orderSpecifications.filterByStatusesAndDate(null, null, null);

        Page<Order> returnedOrderPage = orderRepository.findAll(spec, pageable);

        assertThat(returnedOrderPage).isNotNull();
        assertThat(returnedOrderPage.getTotalElements()).isEqualTo(expectedAmountOfElements);
        assertThat(returnedOrderPage.getTotalPages()).isEqualTo(expectedAmountOfPages);
        assertThat(returnedOrderPage.getContent().size()).isEqualTo(expectedContentSize);
    }

    @Test
    public void findAllOrderStatuses_ReturnsSetOfOrderStatuses() {
        Set<OrderStatus> expectedOrderStatusSet = Set.of(OrderStatus.AWAITING_PAYMENT);

        Set<OrderStatus> returnedOrderStatusSet = orderRepository.findAllOrderStatuses();

        assertThat(returnedOrderStatusSet).isNotNull();
        assertThat(returnedOrderStatusSet).isNotEmpty();
        assertThat(returnedOrderStatusSet).isEqualTo(expectedOrderStatusSet);
    }

    @Test
    public void findAllPaymentStatuses_ReturnsSetOfPaymentStatuses() {
        Set<PaymentStatus> returnedPaymentStatusSet = orderRepository.findAllPaymentStatuses();

        assertThat(returnedPaymentStatusSet).isNotNull();
        assertThat(returnedPaymentStatusSet).isEmpty();
    }
}