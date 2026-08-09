package pl.jaro.restapiworkshop.repository;

import pl.jaro.restapiworkshop.model.Book;

import java.util.Collection;

public interface BookSearchRepository {

    Collection<Book> searchBooks(String search, int page, int pageSize, Long userId);

    long countBySearch(String search, Long userId);
}