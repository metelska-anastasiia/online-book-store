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
import javax.sql.DataSource;
import lombok.SneakyThrows;
import mate.academy.dto.book.BookDtoWithoutCategoryIds;
import mate.academy.dto.category.CategoryDto;
import mate.academy.dto.category.CategoryResponseDto;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.service.CategoryService;
import org.apache.commons.lang3.builder.EqualsBuilder;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CategoryControllerTest {
    private static final int PAGE_NUMBER = 0;
    private static final int PAGE_SIZE = 10;
    private static final Long VALID_ID = 1L;
    private static final Long INVALID_ID = 2000L;
    private static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CategoryService categoryService;

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
                    new ClassPathResource("database/controller/category/remove-from-categories.sql")
            );
        }
    }

    @SneakyThrows
    static void setupDatabase(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/controller/category"
                            + "/add-category-to-categories-table.sql")
            );
        }
    }

    @Test
    @DisplayName("Create a new Category")
    @WithMockUser(username = "admin", password = "test", authorities = {"ADMIN"})
    void createCategory_validCategoryDto_Success() throws Exception {
        CategoryDto requestDto = new CategoryDto()
                .setName("New Category")
                .setDescription("Nre description");

        CategoryResponseDto expected = new CategoryResponseDto()
                .setDescription(requestDto.getDescription())
                .setName(requestDto.getName());

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult mvcResult = mockMvc.perform(
                        post("/api/categories")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();

        CategoryResponseDto actual = objectMapper
                .readValue(jsonResponse, CategoryResponseDto.class);
        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }

    @Test
    @DisplayName("Get all categories from DB")
    @WithMockUser(username = "user", password = "test", authorities = "USER")
    void getAll_WithPagination_ShouldReturnPageWithCategories() throws Exception {
        List<CategoryResponseDto> expected = new ArrayList<>();
        expected.add(new CategoryResponseDto()
                .setName("Poetry")
                .setId(1L)
                .setDescription("Poems that you will love")
        );
        expected.add(new CategoryResponseDto()
                .setName("Fiction")
                .setId(2L)
                .setDescription("Nice fiction to read")
        );

        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);

        MvcResult mvcResult = mockMvc.perform(get("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", String.valueOf(
                                pageable.getPageNumber()))
                        .param("size", String.valueOf(pageable.getPageSize())))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<CategoryResponseDto> actual = objectMapper.readValue(
                jsonResponse,
                new TypeReference<>() {}
        );
        assertNotNull(actual);
        assertEquals(expected.size(), actual.size());
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get category with valid Id from DB")
    @WithMockUser(username = "user", password = "test", authorities = {"USER"})
    void getCategoryById_validId_shouldReturnCategory() throws Exception {
        CategoryResponseDto expected = new CategoryResponseDto()
                .setName("Poetry")
                .setId(1L)
                .setDescription("Poems that you will love");

        MvcResult mvcResult = mockMvc.perform(
                        get("/api/categories/{id}", VALID_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        CategoryResponseDto actual = objectMapper
                .readValue(jsonResponse, CategoryResponseDto.class);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get category with invalid Id throws exception")
    @WithMockUser(username = "user", password = "test", authorities = {"USER"})
    void getBookById_invalidId_shouldReturnException() throws Exception {
        mockMvc.perform(get("/api/categories/{id}", INVALID_ID))
                .andExpect(status().isBadRequest()).andReturn();
        assertThrows(EntityNotFoundException.class, () ->
                categoryService.getById(INVALID_ID));
    }

    @Test
    @DisplayName("Delete with valid Id will delete category")
    @WithMockUser(username = "user", password = "test", authorities = "ADMIN")
    void delete_validId_Success() throws Exception {
        mockMvc.perform(delete("/api/categories/{id}", VALID_ID))
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Test
    @DisplayName("Update category with valid parameters")
    @WithMockUser(username = "user", password = "test", authorities = {"ADMIN"})
    void updateCategory_WithValidIdAndRequestDto_Success() throws Exception {
        CategoryDto updateRequestDto = new CategoryDto()
                .setName("Updated Name")
                .setDescription("Updated description");
        CategoryResponseDto expected = new CategoryResponseDto()
                .setName("Updated Name")
                .setDescription("Updated description")
                .setId(VALID_ID);

        String jsonRequest = objectMapper.writeValueAsString(updateRequestDto);

        MvcResult mvcResult = mockMvc.perform(
                        put("/api/categories/{id}", VALID_ID)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        CategoryResponseDto actual = objectMapper
                .readValue(jsonResponse, CategoryResponseDto.class);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Search books by category id")
    @WithMockUser(username = "user", password = "test", authorities = {"USER"})
    @Sql(scripts = {
            "classpath:database/controller/category/add-books-to-books-table.sql",
            "classpath:database/controller/category/add-category-to-book.sql",
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/controller/category/remove-from-books.sql",
            "classpath:database/controller/category/remove-from-book_category.sql",
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getBooksByCategoryId_validId_success() throws Exception {
        List<BookDtoWithoutCategoryIds> expected = new ArrayList<>();
        expected.add(new BookDtoWithoutCategoryIds()
                .setId(1L)
                .setTitle("Book 1")
                .setAuthor("Author 1")
                .setIsbn("ISBN-123456")
                .setPrice(BigDecimal.valueOf(100))
                .setDescription("Description for Book 1")
                .setCoverImage("image1.jpg"));
        expected.add(new BookDtoWithoutCategoryIds()
                .setId(2L)
                .setTitle("Book 2")
                .setAuthor("Author 2")
                .setIsbn("ISBN-654321")
                .setPrice(BigDecimal.valueOf(200))
                .setDescription("Description for Book 2")
                .setCoverImage("image2.jpg"));
        expected.add(new BookDtoWithoutCategoryIds()
                .setId(3L)
                .setTitle("Book 3")
                .setAuthor("Author 3")
                .setIsbn("ISBN-908765")
                .setPrice(BigDecimal.valueOf(250))
                .setDescription("Description for Book 3")
                .setCoverImage("image3.jpg"));

        MvcResult mvcResult = mockMvc.perform(get("/api/categories/{id}/books", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<BookDtoWithoutCategoryIds> actual = objectMapper
                .readValue(
                        jsonResponse, new TypeReference<>() {}
                );
        assertNotNull(actual);
        assertEquals(expected, actual);
    }
}
