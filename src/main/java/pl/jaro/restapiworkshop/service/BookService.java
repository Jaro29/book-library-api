package pl.jaro.restapiworkshop.service;

import pl.jaro.restapiworkshop.dto.BookCreateRequest;
import pl.jaro.restapiworkshop.dto.PageResponse;
import pl.jaro.restapiworkshop.model.Book;

import java.util.Collection;

public interface BookService {
    Book createBook(BookCreateRequest createRequest, boolean allowDuplicate);

    Book findBookById(Long id);

    PageResponse<Book> findAllBooks(int page, int pageSize);
}
