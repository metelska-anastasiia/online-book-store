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
import mate.academy.repository.cartitem.CartItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CartItemRepositoryIntegrationTest {
    private static final Long CART_ITEM_ID = 1L;
    private static final Long SHOPPING_CART_ID = 1L;
    private static final Long BOOK_ID = 1L;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Test
    @DisplayName("Find cart item by id and shopping cart id")
    @Sql(scripts = {
            "classpath:database/repository/cartItem/after/remove-from-books.sql",
            "classpath:database/repository/cartItem/before/add-books-to-books-table.sql",
            "classpath:database/repository/cartItem/before/add-user-to-users-table.sql",
            "classpath:database/repository/cartItem/before/add-shopping-cart.sql",
            "classpath:database/repository/cartItem/before/add-cart-item-to-cart-items-table.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/repository/cartItem/after/remove-from-cart_items.sql",
            "classpath:database/repository/cartItem/after/remove-from-books.sql",
            "classpath:database/repository/cartItem/after/remove-from-shopping_carts.sql",
            "classpath:database/repository/cartItem/after/remove-from-users.sql",
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findByIdAndShoppingCartId_validIds_Success() {
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

        Optional<CartItem> actual = cartItemRepository
                .findByIdAndShoppingCartId(CART_ITEM_ID, SHOPPING_CART_ID);

        assertFalse(actual.isEmpty());
        assertEquals(expectedBook, actual.get().getBook());
        assertEquals(expectedCartItem, actual.get());

        User user = new User()
                .setId(1L)
                .setEmail("john@test.com")
                .setFirstName("John")
                .setLastName("Doe")
                .setPassword("test");

        ShoppingCart expectedShoppingCart = new ShoppingCart()
                .setId(1L)
                .setCartItems(Set.of(expectedCartItem))
                .setUser(user);

        assertEquals(expectedShoppingCart, actual.get().getShoppingCart());
    }
}
