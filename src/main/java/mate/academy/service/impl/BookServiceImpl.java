package mate.academy.service.impl;

import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import mate.academy.dto.book.BookDto;
import mate.academy.dto.book.BookSearchParameters;
import mate.academy.dto.book.CreateBookRequestDto;
import mate.academy.exception.EntityNotFoundException;
import mate.academy.mapper.BookMapper;
import mate.academy.model.Book;
import mate.academy.repository.book.BookRepository;
import mate.academy.repository.book.BookSpecificationBuilder;
import mate.academy.service.BookService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final BookSpecificationBuilder bookSpecificationBuilder;

    @Override
    public BookDto save(CreateBookRequestDto requestDto) {
        Book book = bookMapper.toModel(requestDto);
        book.setIsbn("ISBN" + new Random().nextInt(100000000));
        return bookMapper.toDto(bookRepository.save(book));
    }

    @Override
    public List<BookDto> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable).stream()
                .map(bookMapper::toDto)
                .toList();
    }

    @Override
    public BookDto findById(Long id) {
        return bookMapper.toDto(bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Can't find book by id " + id)));
    }

    @Override
    public void deleteById(Long id) {
        bookRepository.deleteById(id);
    }

    @Override
    public List<BookDto> search(BookSearchParameters searchParameters) {
        Specification<Book> spec = bookSpecificationBuilder.build(searchParameters);
        return bookRepository.findAll(spec).stream()
                .map(bookMapper::toDto).toList();
    }

    @Override
    public BookDto update(Long id, CreateBookRequestDto requestDto) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Can't find book by id " + id));
        existingBook.setTitle(requestDto.getTitle());
        existingBook.setAuthor(requestDto.getAuthor());
        existingBook.setPrice(requestDto.getPrice());
        existingBook.setDescription(requestDto.getDescription());
        existingBook.setCoverImage(existingBook.getCoverImage());
        Book updatedBookInDb = bookRepository.save(existingBook);
        return bookMapper.toDto(updatedBookInDb);
    }
}
