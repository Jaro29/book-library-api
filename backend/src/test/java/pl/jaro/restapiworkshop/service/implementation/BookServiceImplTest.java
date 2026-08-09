package pl.jaro.restapiworkshop.service.implementation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.jaro.restapiworkshop.dto.BookCreateRequest;
import pl.jaro.restapiworkshop.dto.BookPatchRequest;
import pl.jaro.restapiworkshop.dto.PageResponse;
import pl.jaro.restapiworkshop.exception.BookNotFoundException;
import pl.jaro.restapiworkshop.exception.DuplicateBookException;
import pl.jaro.restapiworkshop.exception.InvalidTimesReadException;
import pl.jaro.restapiworkshop.model.Book;
import pl.jaro.restapiworkshop.model.BookStatus;
import pl.jaro.restapiworkshop.repository.BookRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    private static final Long USER_ID = 1L;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    @Test
    void shouldThrowWhenDuplicateAndNotAllowed() {
        BookCreateRequest request = new BookCreateRequest(
                "Lalka", "Bolesław Prus", null,
                null, null, null, null, null
        );

        when(bookRepository.existsByTitleAndAuthor("Lalka", "Bolesław Prus", USER_ID))
                .thenReturn(true);

        assertThrows(DuplicateBookException.class,
                () -> bookService.createBook(request, false, USER_ID));

        verify(bookRepository, never()).create(any());
    }

    @Test
    void shouldCreateBookWhenDuplicateAllowed() {
        BookCreateRequest request = new BookCreateRequest(
                "Lalka", "Bolesław Prus", null,
                null, null, null, null, null
        );

        when(bookRepository.existsByTitleAndAuthor("Lalka", "Bolesław Prus", USER_ID))
                .thenReturn(true);

        when(bookRepository.create(any())).thenReturn(
                Book.builder().id(1L).title("Lalka").author("Bolesław Prus").build()
        );

        Book result = bookService.createBook(request, true, USER_ID);

        assertEquals(1L, result.getId());
        assertEquals("Lalka", result.getTitle());
        assertEquals("Bolesław Prus", result.getAuthor());

        verify(bookRepository).create(any());
    }

    @Test
    void shouldRejectBookWhenTimesReadIsNegative() {
        BookCreateRequest request = new BookCreateRequest(
                "Lalka", "Bolesław Prus", null,
                null, null, null, -1, null
        );

        when(bookRepository.existsByTitleAndAuthor("Lalka", "Bolesław Prus", USER_ID))
                .thenReturn(false);

        assertThrows(
                InvalidTimesReadException.class,
                () -> bookService.createBook(request, false, USER_ID)
        );
        verify(bookRepository, never()).create(any());
    }

    @Test
    void shouldRejectBookWhenFinishedAndTimesReadIsZero() {
        BookCreateRequest request = new BookCreateRequest(
                "Lalka", "Bolesław Prus", null,
                BookStatus.FINISHED, null, null, 0, null
        );

        assertThrows(
                InvalidTimesReadException.class,
                () -> bookService.createBook(request, false, USER_ID)
        );
        verify(bookRepository, never()).create(any());
    }

    @Test
    void shouldRejectUpdateWhenAnotherBookWithSameTitleAndAuthorExists() {
        Long bookId = 1L;

        Book existingBook = Book.builder()
                .id(bookId)
                .title("Potop")
                .author("Henryk Sienkiewicz")
                .status(BookStatus.TO_READ)
                .timesRead(0)
                .build();

        BookPatchRequest patchRequest = new BookPatchRequest(
                "Lalka", "Bolesław Prus", null,
                null, null, null, null, null
        );

        when(bookRepository.findById(bookId, USER_ID))
                .thenReturn(existingBook);

        when(bookRepository.existsByTitleAndAuthorExcludingId(
                "Lalka", "Bolesław Prus", bookId, USER_ID
        )).thenReturn(true);

        assertThrows(
                DuplicateBookException.class,
                () -> bookService.updateBook(bookId, patchRequest, USER_ID)
        );

        verify(bookRepository, never()).update(any());
    }

    @Test
    void shouldRejectUpdateWhenBookDoesNotExist() {
        Long bookId = 1L;

        when(bookRepository.findById(bookId, USER_ID))
                .thenThrow(new BookNotFoundException("Nie znaleziono książki id: " + bookId));

        BookPatchRequest request = new BookPatchRequest(
                "Lalka", "Bolesław Prus", null,
                null, null, null, null, null
        );

        assertThrows(BookNotFoundException.class,
                () -> bookService.updateBook(bookId, request, USER_ID));

        verify(bookRepository, never()).update(any());
    }

    @Test
    void shouldCalculateTotalPagesWhenNotEvenlyDivisible() {
        int page = 0;
        int pageSize = 10;

        when(bookRepository.findAll(page, pageSize, USER_ID)).thenReturn(List.of());
        when(bookRepository.countAll(USER_ID)).thenReturn(21L);

        PageResponse<Book> result = bookService.findAllBooks(page, pageSize, USER_ID);

        assertEquals(3, result.totalPages());
        assertEquals(21, result.totalElements());
    }

    @Test
    void shouldReturnZeroTotalPagesWhenNoElements() {
        int page = 0;
        int pageSize = 20;

        when(bookRepository.findAll(page, pageSize, USER_ID)).thenReturn(List.of());
        when(bookRepository.countAll(USER_ID)).thenReturn(0L);

        PageResponse<Book> result = bookService.findAllBooks(page, pageSize, USER_ID);

        assertEquals(0, result.totalPages());
        assertEquals(0, result.totalElements());
    }

}