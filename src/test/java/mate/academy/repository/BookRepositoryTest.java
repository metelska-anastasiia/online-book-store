package mate.academy.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import mate.academy.model.Book;
import mate.academy.repository.book.BookRepository;
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
class BookRepositoryTest {
    private static final Long CATEGORY_ID = 1L;
    private static final Long EXPECTED_LIST_SIZE = 3L;
    private static final int PAGE_NUMBER = 0;
    private static final int PAGE_SIZE = 3;

    @Autowired
    private BookRepository bookRepository;

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
}
