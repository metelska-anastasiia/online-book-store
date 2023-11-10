package mate.academy.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
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
    private static final Long BOOK_ID = 1L;
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
        User user = new User()
                .setId(USER_ID)
                .setEmail("john@test.com")
                .setFirstName("John")
                .setLastName("Doe")
                .setPassword("test");

        Book expectedBook = new Book()
                .setId(BOOK_ID)
                .setAuthor("Author 1")
                .setTitle("Book 1")
                .setPrice(BigDecimal.valueOf(100))
                .setIsbn("ISBN-123456")
                .setDescription("Description for Book 1")
                .setCoverImage("image1.jpg");

        CartItem expectedCartItem = new CartItem()
                .setBook(expectedBook)
                .setId(1L)
                .setQuantity(5);

        ShoppingCart expected = new ShoppingCart()
                .setId(1L)
                .setUser(user)
                .setCartItems(Set.of(expectedCartItem));

        Optional<ShoppingCart> actual = shoppingCartRepository
                .findShoppingCartByUserId(USER_ID);

        assertFalse(actual.isEmpty());
        assertEquals(expected, actual.get());
    }
}
