package pl.jaro.restapiworkshop.service.implementation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import pl.jaro.restapiworkshop.dto.BookCreateRequest;
import pl.jaro.restapiworkshop.dto.BookPatchRequest;
import pl.jaro.restapiworkshop.dto.PageResponse;
import pl.jaro.restapiworkshop.exception.BookNotFoundException;
import pl.jaro.restapiworkshop.exception.DuplicateBookException;
import pl.jaro.restapiworkshop.exception.InvalidTimesReadException;
import pl.jaro.restapiworkshop.model.Book;
import pl.jaro.restapiworkshop.model.BookStatus;
import pl.jaro.restapiworkshop.repository.BookRepository;
import pl.jaro.restapiworkshop.rowmapper.BookRowMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static pl.jaro.restapiworkshop.query.BookQuery.SELECT_BOOK_BY_ID_QUERY;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    @Test
    void shouldThrowWhenDuplicateAndNotAllowed() {
        // Arrange
        BookCreateRequest request = new BookCreateRequest(
                "Lalka", "Bolesław Prus", null,
                null, null, null, null, null
        );

        when(bookRepository.existsByTitleAndAuthor("Lalka", "Bolesław Prus"))
                .thenReturn(true);

        // Act + Assert
        assertThrows(DuplicateBookException.class,
                () -> bookService.createBook(request, false));

        verify(bookRepository, never()).create(any());
    }

    @Test
    void shouldCreateBookWhenDuplicateAllowed() {
        // Arrange
        BookCreateRequest request = new BookCreateRequest(
                "Lalka", "Bolesław Prus", null,
                null, null, null, null, null
        );

        when(bookRepository.existsByTitleAndAuthor("Lalka", "Bolesław Prus"))
                .thenReturn(true);

        when(bookRepository.create(any())).thenReturn(
                Book.builder().id(1L).title("Lalka").author("Bolesław Prus").build()
        );

        // Act
        Book result = bookService.createBook(request, true);

        // Assert
        assertEquals(1L, result.getId());
        assertEquals("Lalka", result.getTitle());
        assertEquals("Bolesław Prus", result.getAuthor());

        verify(bookRepository).create(any());
    }

    @Test
    void shouldRejectBookWhenTimesReadIsNegative() {

        // Arrange
        BookCreateRequest request = new BookCreateRequest(
                "Lalka", "Bolesław Prus", null,
                null, null, null, -1, null
        );

        when(bookRepository.existsByTitleAndAuthor(
                "Lalka",
                "Bolesław Prus"
        )).thenReturn(false);

        // Act + Assert
        assertThrows(
                InvalidTimesReadException.class,
                () -> bookService.createBook(request, false)
        );
        verify(bookRepository, never()).create(any());
    }

    @Test
    void shouldRejectBookWhenFinishedAndTimesReadIsZero() {

        // Arrange
        BookCreateRequest request = new BookCreateRequest(
                "Lalka", "Bolesław Prus", null,
                BookStatus.FINISHED, null, null, 0, null
        );

        /*
        Można pominąć, bo Mockito domyślnie zwraca "puste" wartości (false, 0, null, pusta kolekcja) dla niewywołanych when()

        when(bookRepository.existsByTitleAndAuthor(
                "Lalka",
                "Bolesław Prus"
        )).thenReturn(false);
        */

        // Act + Assert
        assertThrows(
                InvalidTimesReadException.class,
                () -> bookService.createBook(request, false)
        );
        verify(bookRepository, never()).create(any());
    }

    @Test
    void shouldRejectUpdateWhenAnotherBookWithSameTitleAndAuthorExists() {

        // Arrange
        Long bookId = 1L;

        Book existingBook = Book.builder()
                .id(bookId)
                .title("Potop")
                .author("Henryk Sienkiewicz")
                .status(BookStatus.TO_READ)
                .timesRead(0)
                .build();

        BookPatchRequest patchRequest = new BookPatchRequest(
                "Lalka",
                "Bolesław Prus",
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(bookRepository.findById(bookId))
                .thenReturn(existingBook);

        when(bookRepository.existsByTitleAndAuthorExcludingId(
                "Lalka",
                "Bolesław Prus",
                bookId
        )).thenReturn(true);

        // Act + Assert
        assertThrows(
                DuplicateBookException.class,
                () -> bookService.updateBook(bookId, patchRequest)
        );

        verify(bookRepository, never()).update(any());
    }

    @Test
    void shouldRejectUpdateWhenBookDoesNotExist() {

        // Arrange
        Long bookId = 1L;

        when(bookRepository.findById(bookId)).thenThrow(new BookNotFoundException("Nie znaleziono książki id: " + bookId));

        BookPatchRequest request = new BookPatchRequest(
                "Lalka", "Bolesław Prus", null,
                null, null, null, null, null
        );

        // Act + Assert
        assertThrows(BookNotFoundException.class,
                () -> bookService.updateBook(bookId, request));

        verify(bookRepository, never()).update(any());
    }

    @Test
    void shouldCalculateTotalPagesWhenNotEvenlyDivisible() {
        // Arrange
        int page = 0;
        int pageSize = 10;

        when(bookRepository.findAll(page, pageSize)).thenReturn(List.of());
        when(bookRepository.countAll()).thenReturn(21);

        // Act
        PageResponse<Book> result = bookService.findAllBooks(page, pageSize);

        // Assert
        assertEquals(3, result.totalPages());
        assertEquals(21, result.totalElements());
    }

    @Test
    void shouldReturnZeroTotalPagesWhenNoElements() {
        // Arrange
        int page = 0;
        int pageSize = 20;

        when(bookRepository.findAll(page, pageSize)).thenReturn(List.of());
        when(bookRepository.countAll()).thenReturn(0);

        // Act
        PageResponse<Book> result = bookService.findAllBooks(page, pageSize);

        // Assert
        assertEquals(0, result.totalPages());
        assertEquals(0, result.totalElements());
    }

}