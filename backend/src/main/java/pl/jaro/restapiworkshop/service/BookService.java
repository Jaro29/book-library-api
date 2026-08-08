package pl.jaro.restapiworkshop.service;

import pl.jaro.restapiworkshop.dto.BookCreateRequest;
import pl.jaro.restapiworkshop.dto.BookPatchRequest;
import pl.jaro.restapiworkshop.dto.PageResponse;
import pl.jaro.restapiworkshop.model.Book;

public interface BookService {
    Book createBook(BookCreateRequest createRequest, boolean allowDuplicate, Long userId);

    Book findBookById(Long id, Long userId);

    PageResponse<Book> findAllBooks(int page, int pageSize, Long userId);

    Book updateBook(Long id, BookPatchRequest bookPatchRequest, Long userId);

    void deleteBook(Long id, Long userId);

    PageResponse<Book> searchBooks(String search, int page, int pageSize, Long userId);
}