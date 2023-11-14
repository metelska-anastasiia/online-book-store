package mate.academy.controller;

import static mate.academy.config.DatabaseHelper.prepareBook;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import mate.academy.dto.cart.ShoppingCartDto;
import mate.academy.dto.cartitem.CartItemQuantityRequestDto;
import mate.academy.dto.cartitem.CartItemRequestDto;
import mate.academy.dto.cartitem.CartItemResponseDto;
import mate.academy.model.Book;
import mate.academy.service.ShoppingCartService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShoppingCartControllerIntegrationTest {
    private static final Long BOOK_ID = 1L;
    private static final int EXPECTED_QTY = 2;
    private static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private WebApplicationContext applicationContext;

    @BeforeAll
    public void beforeAll() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);
    }

    @BeforeEach
    public void setUp() {
        setupDatabase(dataSource);
    }

    @AfterEach
    public void afterEach() {
        teardown(dataSource);
    }

    @SneakyThrows
    private void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/controller/cart/remove-from-shopping_carts.sql")
            );
        }
    }

    @SneakyThrows
    private void setupDatabase(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/controller/cart/add-shopping-cart.sql")
            );
        }
    }

    @Test
    @DisplayName("Add book to shopping cart with valid user")
    @WithMockUser(username = "john@test.com", password = "test", authorities = {"USER"})
    void addBook_validUserAndCartItemRequestDto() throws Exception {
        Book book = prepareBook();
        CartItemRequestDto requestDto = new CartItemRequestDto()
                .setBookId(book.getId())
                .setQuantity(1);

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/cart")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();
    }

    @Test
    @DisplayName("Add book to shopping cart with invalid request")
    @WithMockUser(username = "john@test.com", password = "test", authorities = {"USER"})
    void addBook_validUserAndInvalidCartItemRequestDto() throws Exception {
        CartItemRequestDto requestDto = new CartItemRequestDto()
                .setQuantity(1);

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/cart")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();
    }

    @Test
    @DisplayName("Get all cart items")
    @WithMockUser(username = "john@test.com", password = "test", authorities = {"USER"})
    void getAllCartItems_validAuthentication_Success() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ShoppingCartDto expected = shoppingCartService.getAllCartItems(authentication);
        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        ShoppingCartDto actual = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                ShoppingCartDto.class
        );
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get all cart items with invalid authentication ")
    @WithMockUser(username = "johnny@test.com", password = "invalidtest", authorities = {"USER"})
    void getAllCartItems_invalidAuthentication_BadRequest() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/cart")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();
    }

    @Test
    @DisplayName("Update book qty in shopping cart")
    @WithMockUser(username = "john@test.com", password = "test", authorities = {"USER"})
    void updateCartItemByBookId_validId_Success() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CartItemQuantityRequestDto cartItemQuantityRequestDto = new CartItemQuantityRequestDto();
        cartItemQuantityRequestDto.setQuantity(2);

        String jsonRequest = objectMapper.writeValueAsString(cartItemQuantityRequestDto);
        mockMvc.perform(
                        MockMvcRequestBuilders.put("/api/cart/books/1")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ShoppingCartDto allCartItems = shoppingCartService.getAllCartItems(authentication);
        CartItemResponseDto responseDto = allCartItems.getCartItems().stream()
                .filter(item -> item.getBookId() == BOOK_ID)
                .findFirst()
                .get();
        int actual = responseDto.getQuantity();
        assertEquals(EXPECTED_QTY, actual);
    }

    @Test
    @DisplayName("Delete book by id in shopping cart")
    @WithMockUser(username = "john@test.com", password = "test", authorities = {"USER"})
    void removeCartItemByBookId_validIdAndAuthentication_Success() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/cart/cart-items/1")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
    }
}
