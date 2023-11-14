package mate.academy.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import mate.academy.dto.book.BookDto;
import mate.academy.dto.book.BookDtoWithoutCategoryIds;
import mate.academy.dto.book.CreateBookRequestDto;
import mate.academy.dto.category.CategoryDto;
import mate.academy.dto.category.CategoryResponseDto;
import mate.academy.dto.orderitem.OrderItemResponseDto;
import mate.academy.dto.user.UserRegistrationRequest;
import mate.academy.dto.user.UserResponseDto;
import mate.academy.model.Book;
import mate.academy.model.CartItem;
import mate.academy.model.Category;
import mate.academy.model.Order;
import mate.academy.model.OrderItem;
import mate.academy.model.ShoppingCart;
import mate.academy.model.User;

public class DatabaseHelper {
    private static final Long BOOK_ID = 1L;
    private static final String BOOK_AUTHOR = "Author 1";
    private static final String BOOK_TITLE = "Book 1";
    private static final String BOOK_ISBN = "ISBN-123456";
    private static final String BOOK_DESCRIPTION = "Description for Book 1";
    private static final String BOOK_IMAGE = "image1.jpg";
    private static final BigDecimal BOOK_PRICE = BigDecimal.valueOf(100);
    private static final Long USER_ID = 1L;
    private static final String USER_EMAIL = "john@test.com";
    private static final String USER_FIRSTNAME = "John";
    private static final String USER_LASTNAME = "Doe";
    private static final String USER_PASSWORD = "test";
    private static final Long CATEGORY_ID = 1L;
    private static final String CATEGORY_NAME = "Category 1";
    private static final String CATEGORY_DESCRIPTION = "Test category";
    private static final Long ORDER_ITEM_ID = 1L;
    private static final int ORDER_ITEM_QTY = 1;
    private static final Long SHOPPING_CART_ID = 1L;

    public static UserRegistrationRequest prepareUserRegistrationRequest(
            String email,
            String password,
            String firstName,
            String lastName) {
        return new UserRegistrationRequest()
                .setEmail(email)
                .setPassword(password)
                .setRepeatPassword(password)
                .setFirstName(firstName)
                .setLastName(lastName);
    }

    public static UserRegistrationRequest prepareUserRegistrationRequest() {
        return new UserRegistrationRequest()
                .setEmail(USER_EMAIL)
                .setFirstName(USER_FIRSTNAME)
                .setLastName(USER_LASTNAME)
                .setPassword(USER_PASSWORD)
                .setRepeatPassword(USER_PASSWORD);
    }

    public static UserResponseDto prepareExpectedUserResponse(UserRegistrationRequest request) {
        return new UserResponseDto()
                .setId(3L)
                .setEmail(request.getEmail())
                .setFirstName(request.getFirstName())
                .setLastName(request.getLastName());
    }

    public static BookDto prepareBookDto() {
        return new BookDto()
                .setId(1L)
                .setTitle(BOOK_TITLE)
                .setAuthor(BOOK_AUTHOR)
                .setIsbn(BOOK_ISBN)
                .setPrice(BOOK_PRICE)
                .setDescription(BOOK_DESCRIPTION)
                .setCoverImage(BOOK_IMAGE)
                .setCategoryIds(Set.of());
    }

    public static OrderItemResponseDto prepareOrderItemResponseDto(
            int qty, Long id
    ) {
        return new OrderItemResponseDto()
                .setBookId(BOOK_ID)
                .setQuantity(qty)
                .setId(id);
    }

    public static OrderItemResponseDto prepareOrderItemResponseDto() {
        return new OrderItemResponseDto()
                .setId(ORDER_ITEM_ID)
                .setQuantity(ORDER_ITEM_QTY)
                .setBookId(BOOK_ID);
    }

    public static Book prepareBook() {
        return new Book()
                .setId(BOOK_ID)
                .setAuthor(BOOK_AUTHOR)
                .setTitle(BOOK_TITLE)
                .setPrice(BOOK_PRICE)
                .setIsbn(BOOK_ISBN)
                .setDescription(BOOK_DESCRIPTION)
                .setCoverImage(BOOK_IMAGE);
    }

    public static CartItem prepareCartItem(Book book, Long id, int qty) {
        return new CartItem()
                .setBook(book)
                .setId(id)
                .setQuantity(qty);
    }

    public static User prepareUser() {
        return new User()
                .setId(USER_ID)
                .setEmail(USER_EMAIL)
                .setFirstName(USER_FIRSTNAME)
                .setLastName(USER_LASTNAME)
                .setPassword(USER_PASSWORD);
    }

    public static Order prepareOrder(
            User user,
            Order.Status status,
            Long orderId
    ) {
        return new Order()
                .setId(orderId)
                .setStatus(status)
                .setTotal(BigDecimal.valueOf(450))
                .setOrderDate(LocalDateTime.of(2023, 9, 12, 00, 31, 58))
                .setShippingAddress("Kyiv, NewPost110")
                .setUser(user);
    }

    public static CreateBookRequestDto prepareCreateBookRequestDto() {
        return new CreateBookRequestDto()
                .setTitle(BOOK_TITLE)
                .setAuthor(BOOK_AUTHOR)
                .setIsbn(BOOK_ISBN)
                .setPrice(BOOK_PRICE)
                .setDescription(BOOK_DESCRIPTION)
                .setCoverImage(BOOK_IMAGE)
                .setCategoryIds(Set.of(CATEGORY_ID));
    }

    public static BookDtoWithoutCategoryIds prepareBookDtoWithoutCategories() {
        return new BookDtoWithoutCategoryIds()
                .setId(BOOK_ID)
                .setAuthor(BOOK_AUTHOR)
                .setTitle(BOOK_TITLE)
                .setPrice(BOOK_PRICE)
                .setDescription(BOOK_DESCRIPTION)
                .setCoverImage(BOOK_IMAGE)
                .setIsbn(BOOK_ISBN);
    }

    public static CategoryResponseDto prepareCategoryResponseDto() {
        return new CategoryResponseDto()
                .setId(CATEGORY_ID)
                .setName(CATEGORY_NAME)
                .setDescription(CATEGORY_DESCRIPTION);
    }

    public static Category prepareCategory() {
        return new Category()
                .setId(CATEGORY_ID)
                .setName(CATEGORY_NAME)
                .setDescription(CATEGORY_DESCRIPTION);
    }

    public static CategoryDto prepareCategoryDto() {
        return new CategoryDto()
                .setName(CATEGORY_NAME)
                .setDescription(CATEGORY_DESCRIPTION);
    }

    public static OrderItem prepareOrderItem() {
        return new OrderItem()
                .setId(ORDER_ITEM_ID)
                .setQuantity(ORDER_ITEM_QTY)
                .setBook(prepareBook())
                .setPrice(BOOK_PRICE);
    }

    public static ShoppingCart prepareShoppingCart(User user, Set<CartItem> cartItems) {
        return new ShoppingCart()
                .setUser(user)
                .setId(SHOPPING_CART_ID)
                .setCartItems(cartItems);
    }

    public static UserResponseDto prepareUserResponseDto() {
        return new UserResponseDto()
                .setId(USER_ID)
                .setEmail(USER_EMAIL)
                .setFirstName(USER_FIRSTNAME)
                .setLastName(USER_LASTNAME);
    }
}
