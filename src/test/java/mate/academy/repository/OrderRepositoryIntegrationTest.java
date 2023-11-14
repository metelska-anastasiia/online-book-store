package mate.academy.repository;

import static mate.academy.config.DatabaseHelper.prepareOrder;
import static mate.academy.config.DatabaseHelper.prepareUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;
import mate.academy.model.Order;
import mate.academy.model.User;
import mate.academy.repository.order.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderRepositoryIntegrationTest {
    private static final int PAGE_NUMBER = 0;
    private static final int PAGE_SIZE = 3;
    private static final Long USER_ID = 1L;
    private static final Long ORDER_ID = 2L;
    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("Find all by valid User id")
    @Sql(scripts = {
            "classpath:database/repository/order/after/remove-from-tables.sql",
            "classpath:database/repository/order/before/add-user-to-users-table.sql",
            "classpath:database/repository/order/before/add-orders-to-orders_table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/repository/order/after/remove-from-tables.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findAllByUserId_withPaginationAndValidId_Success() {
        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);
        Page<Order> actual = orderRepository.findAllByUserId(USER_ID, pageable);
        assertNotNull(actual);
        assertEquals(PAGE_SIZE, actual.getSize());
    }

    @Test
    @DisplayName("Find order by valid User id and valid Order id")
    @Sql(scripts = {
            "classpath:database/repository/order/after/remove-from-tables.sql",
            "classpath:database/repository/order/before/add-user-to-users-table.sql",
            "classpath:database/repository/order/before/add-orders-to-orders_table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/repository/order/after/remove-from-tables.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findByUserIdAndId_validIds_Success() {
        User user = prepareUser();
        Order expected = prepareOrder(user, Order.Status.CANCELED, ORDER_ID);

        Optional<Order> actual = orderRepository.findByUserIdAndId(USER_ID, ORDER_ID);

        assertFalse(actual.isEmpty());
        assertEquals(expected, actual.get());
    }

    @Test
    @DisplayName("Find order by Order id")
    @Sql(scripts = {
            "classpath:database/repository/order/after/remove-from-tables.sql",
            "classpath:database/repository/order/before/add-user-to-users-table.sql",
            "classpath:database/repository/order/before/add-orders-to-orders_table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/repository/order/after/remove-from-tables.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findById_validId_Success() {
        User user = prepareUser();
        Order expected = prepareOrder(user, Order.Status.CANCELED, ORDER_ID);
        Optional<Order> actual = orderRepository.findById(ORDER_ID);
        assertFalse(actual.isEmpty());
        assertEquals(expected, actual.get());
    }
}
