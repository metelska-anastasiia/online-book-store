package mate.academy.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import mate.academy.exception.DataProcessingException;
import mate.academy.model.Book;
import mate.academy.repository.BookRepository;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class BookRepositoryImpl implements BookRepository {
    private final EntityManagerFactory entityManagerFactory;

    @Override
    public Book save(Book book) {
        EntityTransaction transaction = null;
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            transaction = entityManager.getTransaction();
            transaction.begin();
            entityManager.persist(book);
            transaction.commit();
            return book;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new DataProcessingException("Can't save book " + book + " to DB", e);
        }
    }

    @Override
    public List<Book> findAll() {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            return entityManager.createQuery("SELECT b FROM Book b", Book.class)
                    .getResultList();
        } catch (Exception e) {
            throw new DataProcessingException("Can't get books from database", e);
        }
    }

    @Override
    public Optional<Book> findById(Long id) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            Book book = entityManager.find(Book.class, id);
            return Optional.ofNullable(book);
        }
    }
}
