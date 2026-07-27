package pl.jaro.restapiworkshop.repository;

import pl.jaro.restapiworkshop.model.Book;
import pl.jaro.restapiworkshop.model.BookStatus;

import java.util.Collection;

public interface BookSearchRepository {

    Book getBookByIsbn(String isbn);

    Collection<Book> getBooksByTitle(String title, int page, int pageSize);

    Collection<Book> getBooksByAuthor(String author, int page, int pageSize);

    Collection<Book> getBooksByStatus(BookStatus status, int page, int pageSize);
}
