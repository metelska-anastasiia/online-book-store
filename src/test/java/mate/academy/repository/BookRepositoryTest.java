package mate.academy.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import mate.academy.dto.book.BookSearchParameters;
import mate.academy.model.Book;
import mate.academy.model.Category;
import mate.academy.repository.book.BookRepository;
import mate.academy.repository.book.BookSpecificationBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryTest {
    private static final Long BOOK_ID = 1L;
    private static final Long CATEGORY_ID = 1L;
    private static final Long EXPECTED_LIST_SIZE = 3L;
    private static final int PAGE_NUMBER = 0;
    private static final int PAGE_SIZE = 3;
    @Autowired
    private BookRepository bookRepository;
    @Mock
    private BookSpecificationBuilder bookSpecificationBuilder;

    @Test
    @DisplayName("Find all books by valid category id")
    @Sql(scripts = {
            "classpath:database/repository/book/before/add-books-to-books-table.sql",
            "classpath:database/repository/book/before/add-category-to-categories-table.sql",
            "classpath:database/repository/book/before/add-category-to-book.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/repository/book/after/remove-from-book_category.sql",
            "classpath:database/repository/book/after/remove-from-books.sql",
            "classpath:database/repository/book/after/remove-from-categories.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findAllByCategoryId_ValidCategoryId_ShouldReturnBooksByCategory() {
        List<Book> actual = bookRepository.findAllByCategoryId(CATEGORY_ID);
        assertFalse(actual.isEmpty());
        assertEquals(EXPECTED_LIST_SIZE, actual.size());
        assertEquals("Book 1", actual.get(0).getTitle());
        assertEquals("Author 1", actual.get(0).getAuthor());
    }

    @Test
    @DisplayName("Find all books with pagination")
    @Sql(scripts = {
            "classpath:database/repository/book/before/add-books-to-books-table.sql",
            "classpath:database/repository/book/before/add-category-to-categories-table.sql",
            "classpath:database/repository/book/before/add-category-to-book.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/repository/book/after/remove-from-book_category.sql",
            "classpath:database/repository/book/after/remove-from-books.sql",
            "classpath:database/repository/book/after/remove-from-categories.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findAll_WithPagination_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);
        Page<Book> actual = bookRepository.findAll(pageable);
        assertFalse(actual.isEmpty());
        assertEquals(PAGE_SIZE, actual.getContent().size());
    }

    @Test
    @DisplayName("Find book with valid id represents book with categories")
    @Sql(scripts = {
            "classpath:database/repository/book/before/add-books-to-books-table.sql",
            "classpath:database/repository/book/before/add-category-to-categories-table.sql",
            "classpath:database/repository/book/before/add-category-to-book.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/repository/book/after/remove-from-book_category.sql",
            "classpath:database/repository/book/after/remove-from-books.sql",
            "classpath:database/repository/book/after/remove-from-categories.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findById_validId_ShouldReturnBook() {
        Book expected = new Book()
                .setId(1L)
                .setTitle("Book 1")
                .setAuthor("Author 1")
                .setIsbn("ISBN-123456")
                .setPrice(BigDecimal.valueOf(100))
                .setDescription("Description for Book 1")
                .setCoverImage("image1.jpg")
                .setCategories(Set.of(new Category()
                        .setId(1L)
                        .setName("Poetry")
                        .setDescription("Poems that you will love"))
        );
        Optional<Book> actual = bookRepository.findById(BOOK_ID);
        assertFalse(actual.isEmpty());
        assertEquals(expected, actual.get());
    }

    @Test
    @DisplayName("Find all books by search parameters")
    @Sql(scripts = {
            "classpath:database/repository/book/before/add-books-to-books-table.sql",
            "classpath:database/repository/book/before/add-category-to-categories-table.sql",
            "classpath:database/repository/book/before/add-category-to-book.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/repository/book/after/remove-from-book_category.sql",
            "classpath:database/repository/book/after/remove-from-books.sql",
            "classpath:database/repository/book/after/remove-from-categories.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findAll_withSpecification_Success() {

        BookSearchParameters bookSearchParameters = new BookSearchParameters(
                new String[]{"Author 1"}, new String[]{"%Book%"});

        Specification<Book> spec = (root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("author"), "Author 1"));
            predicate = builder.and(predicate, builder.like(root.get("title"), "%Book%"));
            return predicate;
        };

        when(bookSpecificationBuilder.build(bookSearchParameters)).thenReturn(spec);

        List<Book> expected = new ArrayList<>();
        expected.add(
                new Book().setId(1L)
                        .setTitle("Book 1")
                        .setAuthor("Author 1")
                        .setIsbn("ISBN-123456")
                        .setPrice(BigDecimal.valueOf(100))
                        .setDescription("Description for Book 1")
                        .setCoverImage("image1.jpg")
                        .setCategories(Set.of(new Category()
                                .setId(1L)
                                .setName("Poetry")
                                .setDescription("Poems that you will love"))
                        )
        );

        List<Book> actual = bookRepository.findAll(spec);

        assertEquals(expected, actual);
    }
}
