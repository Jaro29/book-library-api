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

    @Mock
    private NamedParameterJdbcTemplate jdbc;

    private BookRepositoryImpl bookRepository;

    @BeforeEach
    void setUp() {
        bookRepository = new BookRepositoryImpl(jdbc);
    }

    @Test
    void shouldThrowBookNotFoundExceptionWhenBookDoesNotExist() {

        // Arrange
        Long id = 1L;

        when(jdbc.queryForObject(
                eq(SELECT_BOOK_BY_ID_QUERY),
                eq(Map.of("id", id)),
                any(BookRowMapper.class)
        )).thenThrow(new EmptyResultDataAccessException(1));

        // Act + Assert
        assertThrows(
                BookNotFoundException.class,
                () -> bookRepository.findById(id)
        );
    }

    @Test
    void shouldThrowApiExceptionWhenDatabaseErrorOccurs() {

        // Arrange
        Long id = 1L;

        when(jdbc.queryForObject(
                eq(SELECT_BOOK_BY_ID_QUERY),
                eq(Map.of("id", id)),
                any(BookRowMapper.class)
        )).thenThrow(new RuntimeException("Database connection error"));

        // Act + Assert
        assertThrows(
                ApiException.class,
                () -> bookRepository.findById(id)
        );
    }

    @Test
    void shouldReturnBookWhenFound() {

        // Arrange
        Long id = 1L;

        Book book = new Book();
        book.setId(id);
        book.setTitle("Lalka");
        book.setAuthor("Bolesław Prus");

        when(jdbc.queryForObject(
                eq(SELECT_BOOK_BY_ID_QUERY),
                eq(Map.of("id", id)),
                any(BookRowMapper.class)
        )).thenReturn(book);


        // Act
        Book result = bookRepository.findById(id);


        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getTitle()).isEqualTo("Lalka");
    }
}
