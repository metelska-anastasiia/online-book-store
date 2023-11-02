package mate.academy.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import mate.academy.dto.book.BookDto;
import mate.academy.dto.book.CreateBookRequestDto;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.service.BookService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookControllerTest {
    private static final int PAGE_NUMBER = 0;
    private static final int PAGE_SIZE = 10;
    private static final Long VALID_ID = 1L;
    private static final Long INVALID_ID = 100000L;
    private static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private BookService bookService;

    @BeforeAll
    static void beforeAll(
            @Autowired DataSource dataSource,
            @Autowired WebApplicationContext applicationContext
    ) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);
    }

    @BeforeEach
    void setUp(@Autowired DataSource dataSource) {
        setupDatabase(dataSource);
    }

    @AfterEach
    void afterEach(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/controller/book/remove-from-books.sql")
            );
        }
    }

    @SneakyThrows
    static void setupDatabase(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/controller/book/add-books-to-books-table.sql")
            );
        }
    }

    @Test
    @DisplayName("Create a new Book")
    @WithMockUser(username = "admin", password = "test", authorities = {"ADMIN", "USER"})
    void createBook_validCreateBookRequestDto_Success() throws Exception {
        //Given
        CreateBookRequestDto createBookRequestDto = new CreateBookRequestDto()
                .setTitle("Create Method")
                .setAuthor("Author 1")
                .setIsbn("12345")
                .setPrice(BigDecimal.valueOf(200))
                .setDescription("New book to test")
                .setCoverImage("image.jpg");
        BookDto expected = new BookDto()
                .setAuthor(createBookRequestDto.getAuthor())
                .setTitle(createBookRequestDto.getTitle())
                .setPrice(createBookRequestDto.getPrice())
                .setDescription(createBookRequestDto.getDescription())
                .setCoverImage(createBookRequestDto.getCoverImage())
                .setIsbn(createBookRequestDto.getIsbn());

        String jsonRequest = objectMapper.writeValueAsString(createBookRequestDto);

        //When
        MvcResult mvcResult = mockMvc.perform(
                post("/api/books")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andReturn();

        //Then
        BookDto actual = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(),
                BookDto.class);
        assertNotNull(actual);
        assertNotNull(actual.getId());
        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }

    @Test
    @DisplayName("Verify getAll() represents all books from DB")
    @WithMockUser(username = "user", password = "test", authorities = "USER")
    void getAll_WithPagination_ShouldReturnPageWithBooks() throws Exception {
        //Given
        List<BookDto> expected = new ArrayList<>();
        expected.add(new BookDto()
                .setId(1L)
                .setTitle("Book 1")
                .setAuthor("Author 1")
                .setIsbn("ISBN-123456")
                .setPrice(BigDecimal.valueOf(100))
                .setDescription("Description for Book 1")
                .setCoverImage("image1.jpg")
                .setCategoryIds(Set.of()));
        expected.add(new BookDto()
                .setId(2L)
                .setTitle("Book 2")
                .setAuthor("Author 2")
                .setIsbn("ISBN-654321")
                .setPrice(BigDecimal.valueOf(200))
                .setDescription("Description for Book 2")
                .setCoverImage("image2.jpg")
                .setCategoryIds(Set.of()));
        expected.add(new BookDto()
                .setId(3L)
                .setTitle("Book 3")
                .setAuthor("Author 3")
                .setIsbn("ISBN-908765")
                .setPrice(BigDecimal.valueOf(250))
                .setDescription("Description for Book 3")
                .setCoverImage("image3.jpg")
                .setCategoryIds(Set.of()));

        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);

        //When
        MvcResult mvcResult = mockMvc.perform(get("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", String.valueOf(
                                pageable.getPageNumber()))
                        .param("size", String.valueOf(pageable.getPageSize())))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();

        //Then
        List<BookDto> actual = objectMapper.readValue(
                jsonResponse,
                new TypeReference<>() {}
        );
        assertNotNull(actual);
        assertEquals(expected.size(), actual.size());
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify getBookById() with valid Id returns book from DB")
    @WithMockUser(username = "user", password = "test", authorities = {"USER", "ADMIN"})
    void getBookById_validId_shouldReturnBook() throws Exception {
        //Given
        BookDto expected = new BookDto()
                .setId(1L)
                .setTitle("Book 1")
                .setAuthor("Author 1")
                .setIsbn("ISBN-123456")
                .setPrice(BigDecimal.valueOf(100))
                .setDescription("Description for Book 1")
                .setCoverImage("image1.jpg")
                .setCategoryIds(Set.of());

        // When
        MvcResult mvcResult = mockMvc.perform(
                get("/api/books/{id}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        // Then
        BookDto actual = objectMapper.readValue(jsonResponse, BookDto.class);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify getBookById() with invalid Id throws exception")
    @WithMockUser(username = "user", password = "test", authorities = {"USER", "ADMIN"})
    void getBookById_invalidId_shouldReturnException() throws Exception {
        mockMvc.perform(get("/api/books/{id}", INVALID_ID))
                .andExpect(status().isBadRequest()).andReturn();
        assertThrows(EntityNotFoundException.class, () -> bookService.findById(
                INVALID_ID));
    }

    @Test
    @DisplayName("Verify delete() with valid will delete book")
    @WithMockUser(username = "user", password = "test", authorities = "ADMIN")
    void delete_validId_Success() throws Exception {
        mockMvc.perform(delete("/api/books/{id}", VALID_ID))
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Test
    @DisplayName("Verify search() with valid parameters will return book")
    @WithMockUser(username = "user", password = "test", authorities = {"ADMIN", "USER"})
    void search_validSearchParameters_Success() throws Exception {
        List<BookDto> expected = new ArrayList<>();
        expected.add(new BookDto()
                 .setId(1L)
                 .setTitle("Book 1")
                 .setAuthor("Author 1")
                 .setIsbn("ISBN-123456")
                 .setPrice(BigDecimal.valueOf(100))
                 .setDescription("Description for Book 1")
                 .setCoverImage("image1.jpg")
                 .setCategoryIds(Set.of()));

        MvcResult mvcResult = mockMvc.perform(
                        get("/api/books/search?titles=Book 1&authors=Author 1")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();

        List<BookDto> actual = objectMapper.readValue(
                jsonResponse,
                new TypeReference<>() {}
        );
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Update book with valid parameters")
    @WithMockUser(username = "user", password = "test", authorities = {"ADMIN", "USER"})
    void updateBook_WithValidIdAndCreateBookRequestDto_Success() throws Exception {
        CreateBookRequestDto updateBookRequestDto = new CreateBookRequestDto()
                .setTitle("Updated Title")
                .setAuthor("Updated Author")
                .setIsbn("12345")
                .setPrice(BigDecimal.valueOf(250))
                .setDescription("Updated description")
                .setCoverImage("updated_image.jpg");

        BookDto expected = new BookDto()
                .setTitle(updateBookRequestDto.getTitle())
                .setAuthor(updateBookRequestDto.getAuthor())
                .setIsbn(updateBookRequestDto.getIsbn())
                .setPrice(updateBookRequestDto.getPrice())
                .setDescription(updateBookRequestDto.getDescription())
                .setCoverImage(updateBookRequestDto.getCoverImage())
                .setId(VALID_ID);

        String jsonRequest = objectMapper.writeValueAsString(updateBookRequestDto);

        MvcResult mvcResult = mockMvc.perform(
                        put("/api/books/{id}", VALID_ID)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        BookDto actual = objectMapper.readValue(jsonResponse, BookDto.class);
        assertNotNull(actual);
        EqualsBuilder.reflectionEquals(expected, actual);
    }
}
