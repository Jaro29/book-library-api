package pl.jaro.restapiworkshop.repository;

import pl.jaro.restapiworkshop.model.Book;
import pl.jaro.restapiworkshop.model.BookSort;
import pl.jaro.restapiworkshop.model.BookStatus;

import java.util.Collection;

public interface BookRepository {

    boolean existsByTitleAndAuthor(String title, String author, Long userId);

    boolean existsByTitleAndAuthorExcludingId(String title, String author, Long id, Long userId);

    Book create(Book book);

    Collection<Book> findAll(BookStatus status, BookSort sort, int page, int pageSize, Long userId);

    Book findById(Long id, Long userId);

    Book update(Book book);

    void delete(Long id, Long userId);

    long countAll(BookStatus status, Long userId);

}