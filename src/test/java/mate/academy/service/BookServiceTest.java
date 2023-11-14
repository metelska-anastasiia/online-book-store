package mate.academy.service;

import static mate.academy.config.DatabaseHelper.prepareBook;
import static mate.academy.config.DatabaseHelper.prepareBookDto;
import static mate.academy.config.DatabaseHelper.prepareBookDtoWithoutCategories;
import static mate.academy.config.DatabaseHelper.prepareCreateBookRequestDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import mate.academy.dto.book.BookDto;
import mate.academy.dto.book.BookDtoWithoutCategoryIds;
import mate.academy.dto.book.BookSearchParameters;
import mate.academy.dto.book.CreateBookRequestDto;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.mapper.BookMapper;
import mate.academy.model.Book;
import mate.academy.repository.book.BookRepository;
import mate.academy.repository.book.BookSpecificationBuilder;
import mate.academy.service.impl.BookServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {
    private static final Long BOOK_ID = 1L;
    private static final Long INVALID_BOOK_ID = -1000L;
    private static final String BOOK_AUTHOR_1 = "Author 1";
    private static final String BOOK_TITLE_1 = "Book 1";
    @Mock
    private BookRepository bookRepository;
    @Mock
    private BookMapper bookMapper;
    @Mock
    private BookSpecificationBuilder bookSpecificationBuilder;
    @InjectMocks
    private BookServiceImpl bookService;

    @Test
    @DisplayName("Verify save() method. Correct book returns after saving")
    void save_ValidCreateBookRequestDto_ShouldSaveBook() {
        //Given
        CreateBookRequestDto createBookRequestDto = prepareCreateBookRequestDto();

        BookDto expected = prepareBookDto();
        Book book = prepareBook();

        when(bookMapper.toModel(createBookRequestDto)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(expected);

        //When
        BookDto actual = bookService.save(createBookRequestDto);

        //Then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify findAll() displays all books")
    void findAll_validPageable_ReturnsAllBooks() {
        //Given
        Pageable pageable = PageRequest.of(0, 10);

        BookDto bookDto = prepareBookDto();
        List<BookDto> expected = new ArrayList<>();
        expected.add(bookDto);
        Book book = prepareBook();

        List<Book> books = List.of(book);
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());

        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        when(bookMapper.toDto(book)).thenReturn(bookDto);

        //When
        List<BookDto> actual = bookService.findAll(pageable);

        //Then
        assertEquals(expected, actual);

        verify(bookRepository, times(1)).findAll(pageable);
        verify(bookMapper, times(1)).toDto(book);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("Verify findById() displays right book")
    void findById_ValidBookId_ShouldFindBook() {
        //Given
        BookDto expected = prepareBookDto();
        Book book = prepareBook();

        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(book));
        when(bookMapper.toDto(book)).thenReturn(expected);

        //When
        BookDto actual = bookService.findById(BOOK_ID);

        //Then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify findById() displays right message when there is no such book")
    void findById_InvalidBookId_ShouldThrowEntityNotFoundException() {
        //Given
        when(bookRepository.findById(INVALID_BOOK_ID))
                .thenThrow(new EntityNotFoundException("Can't find book by id " + INVALID_BOOK_ID));

        //When
        EntityNotFoundException entityNotFoundException = assertThrows(
                EntityNotFoundException.class,
                () -> bookService.findById(INVALID_BOOK_ID)
        );

        //Then
        assertEquals("Can't find book by id " + INVALID_BOOK_ID,
                entityNotFoundException.getMessage());
        assertEquals(EntityNotFoundException.class, entityNotFoundException.getClass());
    }

    @Test
    @DisplayName("Verify deleteById() deletes book by id")
    void deleteById_ValidBookId_ShouldDeleteBook() {
        doNothing().when(bookRepository).deleteById(BOOK_ID);
        bookService.deleteById(BOOK_ID);
        verify(bookRepository, times(1)).deleteById(BOOK_ID);
    }

    @Test
    @DisplayName("Verify search() searches books by input parameters")
    void search_ValidSearchParameters_ShouldReturnListOfBooks() {
        String[] authorParams = {BOOK_AUTHOR_1, "Author 2"};
        String[] titleParams = {BOOK_TITLE_1, "Test Book 2"};

        BookSearchParameters searchParameters =
                new BookSearchParameters(authorParams, titleParams);

        Specification<Book> specification = bookSpecificationBuilder.build(searchParameters);
        Book book = prepareBook();
        Book secondBook = new Book()
                .setId(2L)
                .setAuthor("Author 2")
                .setTitle("Test Book 2")
                .setPrice(BigDecimal.valueOf(340))
                .setDescription("New book2 to test")
                .setCoverImage("image.jpg")
                .setIsbn("12345678");

        List<Book> mockBooks = new ArrayList<>();
        mockBooks.add(book);
        mockBooks.add(secondBook);

        when(bookRepository.findAll(
                specification)).thenReturn(mockBooks);

        List<BookDto> expected = mockBooks.stream().map(bookMapper::toDto).toList();
        List<BookDto> actual = bookService.search(searchParameters);

        assertEquals(2, actual.size());
        assertEquals(expected, actual);
        verify(bookRepository, times(1)).findAll(specification);
    }

    @Test
    @DisplayName("Verify update() updated books with valid ID and input parameters")
    void update_ValidIdAndRequestParameters_ShouldUpdateBook() {
        //Given
        CreateBookRequestDto bookRequestDto = new CreateBookRequestDto()
                .setAuthor("New Author")
                .setTitle("New name")
                .setPrice(BigDecimal.valueOf(199))
                .setDescription("New description to update the book")
                .setCoverImage("new_image.jpg")
                .setIsbn("54321");

        Book updatedBook = new Book();
        updatedBook.setId(BOOK_ID);
        updatedBook.setTitle(bookRequestDto.getTitle());
        updatedBook.setAuthor(bookRequestDto.getAuthor());
        updatedBook.setPrice(bookRequestDto.getPrice());
        updatedBook.setDescription(bookRequestDto.getDescription());
        updatedBook.setCoverImage(bookRequestDto.getCoverImage());
        updatedBook.setIsbn(bookRequestDto.getIsbn());

        BookDto expected = bookMapper.toDto(updatedBook);
        Book book = prepareBook();

        when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);

        //When
        BookDto actual = bookService.update(BOOK_ID, bookRequestDto);

        //Then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify update() throws exception for invalid ID")
    void update_InvalidId_ShouldThrowEntityNotFoundException() {
        //Given
        when(bookRepository.findById(INVALID_BOOK_ID))
                .thenThrow(new EntityNotFoundException("Can't find book by id " + INVALID_BOOK_ID));
        CreateBookRequestDto bookRequestDto = new CreateBookRequestDto();

        //When
        EntityNotFoundException entityNotFoundException = assertThrows(
                EntityNotFoundException.class,
                () -> bookService.update(INVALID_BOOK_ID, bookRequestDto)
        );

        //Then
        assertEquals("Can't find book by id " + INVALID_BOOK_ID,
                entityNotFoundException.getMessage());
        assertEquals(EntityNotFoundException.class, entityNotFoundException.getClass());
    }

    @Test
    void findAllByCategoryId_validCategoryId_ShouldReturnListOfBooks() {
        //Given
        List<Book> bookList = new ArrayList<>();
        bookList.add(prepareBook());

        BookDtoWithoutCategoryIds bookDtoWithoutCategoryIds = prepareBookDtoWithoutCategories();

        List<BookDtoWithoutCategoryIds> expected = Arrays.asList(
                bookDtoWithoutCategoryIds
        );

        when(bookRepository.findAllByCategoryId(anyLong())).thenReturn(bookList);
        when(bookMapper.toDtoWithoutCategories(any(Book.class))).thenReturn(expected.get(0));

        //When
        List<BookDtoWithoutCategoryIds> actual = bookService.findAllByCategoryId(1L);

        //Then
        assertEquals(expected, actual);
    }
}
