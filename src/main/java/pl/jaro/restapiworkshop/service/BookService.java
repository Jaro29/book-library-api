package pl.jaro.restapiworkshop.service;

import pl.jaro.restapiworkshop.dto.BookCreateRequest;
import pl.jaro.restapiworkshop.model.Book;

public interface BookService {
    Book createBook(BookCreateRequest createRequest, boolean allowDuplicate);
}
