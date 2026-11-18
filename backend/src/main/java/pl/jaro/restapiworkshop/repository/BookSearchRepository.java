package pl.jaro.restapiworkshop.repository;

import pl.jaro.restapiworkshop.model.Book;
import pl.jaro.restapiworkshop.model.BookSort;
import pl.jaro.restapiworkshop.model.BookStatus;

import java.util.Collection;

public interface BookSearchRepository {

    Collection<Book> searchBooks(String search, BookStatus status, BookSort sort, int page, int pageSize, Long userId);

    long countBySearch(String search, BookStatus status, Long userId);
}