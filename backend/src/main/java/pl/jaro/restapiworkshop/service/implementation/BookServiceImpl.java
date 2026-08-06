package pl.jaro.restapiworkshop.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.jaro.restapiworkshop.dto.BookCreateRequest;
import pl.jaro.restapiworkshop.dto.BookPatchRequest;
import pl.jaro.restapiworkshop.dto.PageResponse;
import pl.jaro.restapiworkshop.exception.ApiException;
import pl.jaro.restapiworkshop.exception.DuplicateBookException;
import pl.jaro.restapiworkshop.exception.InvalidTimesReadException;
import pl.jaro.restapiworkshop.mapper.BookMapper;
import pl.jaro.restapiworkshop.model.Book;
import pl.jaro.restapiworkshop.model.BookStatus;
import pl.jaro.restapiworkshop.repository.BookRepository;
import pl.jaro.restapiworkshop.repository.BookSearchRepository;
import pl.jaro.restapiworkshop.service.BookService;

import java.util.ArrayList;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookSearchRepository bookSearchRepository;

    @Override
    public Book createBook(BookCreateRequest createRequest, boolean allowDuplicate) {
        boolean exists = bookRepository.existsByTitleAndAuthor(createRequest.title(), createRequest.author());
        if (!allowDuplicate && exists) {
            throw new DuplicateBookException("Książka o tym tytule i autorze już istnieje.");
        }

        Book book = BookMapper.toBook(createRequest);
        validateTimesRead(book);
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
        Book existingBook = bookRepository.findById(id);
        Book updatedBook = BookMapper.toBook(patchRequest, existingBook);
        validateTimesRead(updatedBook);
        if (bookRepository.existsByTitleAndAuthorExcludingId(updatedBook.getTitle(), updatedBook.getAuthor(), id)) {
            throw new DuplicateBookException("Inna książka o tym tytule i autorze już istnieje.");
        }

        return bookRepository.update(updatedBook);
    }

    @Override
    public void deleteBook(Long id) {
        bookRepository.delete(id);
    }

    @Override
    public PageResponse<Book> searchBooks(String search, int page, int pageSize) {
        Collection<Book> books = bookSearchRepository.searchBooks(search, page, pageSize);
        int totalElements = bookSearchRepository.countBySearch(search);

        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

        return new PageResponse<>(new ArrayList<>(books), page, pageSize, totalElements, totalPages);
    }

    private void validateTimesRead(Book book) {
        if (book.getTimesRead() < 0) {
            throw new InvalidTimesReadException("Liczba przeczytań nie może być ujemna.");
        }
        if ((book.getStatus() == BookStatus.FINISHED) && book.getTimesRead() == 0) {
            throw new InvalidTimesReadException("Wpisz przynajmniej 1 jeśli książka przeczytana.");
        }
    }

}
