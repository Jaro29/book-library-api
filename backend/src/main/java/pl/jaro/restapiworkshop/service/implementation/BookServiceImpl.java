package pl.jaro.restapiworkshop.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.jaro.restapiworkshop.dto.BookCreateRequest;
import pl.jaro.restapiworkshop.dto.BookPatchRequest;
import pl.jaro.restapiworkshop.dto.PageResponse;
import pl.jaro.restapiworkshop.exception.ApiException;
import pl.jaro.restapiworkshop.mapper.BookMapper;
import pl.jaro.restapiworkshop.model.Book;
import pl.jaro.restapiworkshop.repository.BookRepository;
import pl.jaro.restapiworkshop.service.BookService;

import java.util.ArrayList;
import java.util.Collection;

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

    @Override
    public Book findBookById(Long id) {
        return bookRepository.findById(id);
    }

    @Override
    public PageResponse<Book> findAllBooks(int page, int pageSize) {
        Collection<Book> books = bookRepository.findAll(page, pageSize);
        int totalElements = bookRepository.countAll();

        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

        return new PageResponse<>(new ArrayList<>(books), page, pageSize, totalElements, totalPages);
    }

    @Override
    public Book updateBook(Long id, BookPatchRequest patchRequest) {
        Book book = BookMapper.toBook(patchRequest, bookRepository.findById(id));
        return bookRepository.update(book);
    }

    @Override
    public void deleteBook(Long id) {
        bookRepository.delete(id);
    }

}
