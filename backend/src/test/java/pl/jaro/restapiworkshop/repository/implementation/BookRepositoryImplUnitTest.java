package pl.jaro.restapiworkshop.repository.implementation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import pl.jaro.restapiworkshop.exception.ApiException;
import pl.jaro.restapiworkshop.exception.BookNotFoundException;
import pl.jaro.restapiworkshop.model.Book;
import pl.jaro.restapiworkshop.rowmapper.BookRowMapper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static pl.jaro.restapiworkshop.query.BookQuery.SELECT_BOOK_BY_ID_QUERY;

@ExtendWith(MockitoExtension.class)
public class BookRepositoryImplUnitTest {

    private static final Long USER_ID = 1L;

    @Mock
    private NamedParameterJdbcTemplate jdbc;

    private BookRepositoryImpl bookRepository;

    @BeforeEach
    void setUp() {
        bookRepository = new BookRepositoryImpl(jdbc);
    }

    @Test
    void shouldThrowBookNotFoundExceptionWhenBookDoesNotExist() {
        Long id = 1L;

        when(jdbc.queryForObject(
                eq(SELECT_BOOK_BY_ID_QUERY),
                eq(Map.of("id", id, "userId", USER_ID)),
                any(BookRowMapper.class)
        )).thenThrow(new EmptyResultDataAccessException(1));

        assertThrows(
                BookNotFoundException.class,
                () -> bookRepository.findById(id, USER_ID)
        );
    }

    @Test
    void shouldThrowApiExceptionWhenDatabaseErrorOccurs() {
        Long id = 1L;

        when(jdbc.queryForObject(
                eq(SELECT_BOOK_BY_ID_QUERY),
                eq(Map.of("id", id, "userId", USER_ID)),
                any(BookRowMapper.class)
        )).thenThrow(new RuntimeException("Database connection error"));

        assertThrows(
                ApiException.class,
                () -> bookRepository.findById(id, USER_ID)
        );
    }

    @Test
    void shouldReturnBookWhenFound() {
        Long id = 1L;

        Book book = new Book();
        book.setId(id);
        book.setTitle("Lalka");
        book.setAuthor("Bolesław Prus");

        when(jdbc.queryForObject(
                eq(SELECT_BOOK_BY_ID_QUERY),
                eq(Map.of("id", id, "userId", USER_ID)),
                any(BookRowMapper.class)
        )).thenReturn(book);

        Book result = bookRepository.findById(id, USER_ID);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getTitle()).isEqualTo("Lalka");
    }
}