package pl.jaro.restapiworkshop.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.jaro.restapiworkshop.dto.BookCreateRequest;
import pl.jaro.restapiworkshop.exception.ApiException;
import pl.jaro.restapiworkshop.mapper.BookMapper;
import pl.jaro.restapiworkshop.model.Book;
import pl.jaro.restapiworkshop.repository.BookRepository;
import pl.jaro.restapiworkshop.service.BookService;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    @Override
    public Book createBook(BookCreateRequest createRequest, boolean allowDuplicate) {
        boolean exists = bookRepository.existsByTitleAndAuthor(createRequest.title(), createRequest.author());
        if (!allowDuplicate && exists) {
            throw new ApiException("Książka o tym tytule i autorze już istnieje.");
        }

        Book book = BookMapper.toBook(createRequest);

        return bookRepository.create(book);
    }
}
