package pl.jaro.restapiworkshop.repository;

import pl.jaro.restapiworkshop.model.Book;
import pl.jaro.restapiworkshop.model.BookStatus;

import java.util.Collection;

public interface BookRepository {

    boolean existsByTitleAndAuthor(String title, String author);

    Book create(Book book);

    Collection<Book> findAll(int page, int pageSize);

    Book findById(Long id);

    Book update(Book book);

    void delete(Long id);

    int countAll();

}
