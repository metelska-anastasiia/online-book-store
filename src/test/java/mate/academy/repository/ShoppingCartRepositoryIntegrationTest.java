package mate.academy.repository;

import static mate.academy.config.DatabaseHelper.prepareBook;
import static mate.academy.config.DatabaseHelper.prepareCartItem;
import static mate.academy.config.DatabaseHelper.prepareShoppingCart;
import static mate.academy.config.DatabaseHelper.prepareUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Optional;
import java.util.Set;
import mate.academy.model.Book;
import mate.academy.model.CartItem;
import mate.academy.model.ShoppingCart;
import mate.academy.model.User;
import mate.academy.repository.cart.ShoppingCartRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ShoppingCartRepositoryIntegrationTest {
    private static final Long USER_ID = 1L;
    private static final Long CART_ITEM_ID = 1L;
    private static final int QUANTITY = 5;
    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Test
    @Sql(scripts = {
            "classpath:database/repository/cart/before/add-books-to-books-table.sql",
            "classpath:database/repository/cart/before/add-user-to-users-table.sql",
            "classpath:database/repository/cart/before/add-shopping-cart.sql",
            "classpath:database/repository/cart/before/add-cart-item-to-cart-items-table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/repository/cart/after/remove-from-cart_items.sql",
            "classpath:database/repository/cart/after/remove-from-books.sql",
            "classpath:database/repository/cart/after/remove-from-shopping_carts.sql",
            "classpath:database/repository/cart/after/remove-from-users.sql",
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findShoppingCartByUserId_validId_Success() {
        User user = prepareUser();
        Book expectedBook = prepareBook();
        CartItem expectedCartItem = prepareCartItem(expectedBook, CART_ITEM_ID, QUANTITY);
        ShoppingCart expected = prepareShoppingCart(user, Set.of(expectedCartItem));

        Optional<ShoppingCart> actual = shoppingCartRepository
                .findShoppingCartByUserId(USER_ID);

        assertFalse(actual.isEmpty());
        assertEquals(expected, actual.get());
    }
}
