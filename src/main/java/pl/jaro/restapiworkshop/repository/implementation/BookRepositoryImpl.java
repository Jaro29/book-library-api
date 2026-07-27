package pl.jaro.restapiworkshop.repository.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import pl.jaro.restapiworkshop.exception.ApiException;
import pl.jaro.restapiworkshop.exception.BookNotFoundException;
import pl.jaro.restapiworkshop.model.Book;
import pl.jaro.restapiworkshop.model.BookStatus;
import pl.jaro.restapiworkshop.repository.BookRepository;
import pl.jaro.restapiworkshop.repository.BookSearchRepository;
import pl.jaro.restapiworkshop.rowmapper.BookRowMapper;

import java.util.Collection;
import java.util.Map;

import static java.util.Map.of;
import static java.util.Objects.requireNonNull;
import static pl.jaro.restapiworkshop.query.BookQuery.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class BookRepositoryImpl implements BookRepository, BookSearchRepository {

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public boolean existsByTitleAndAuthor(String title, String author) {
        try {
            Integer count = jdbc.queryForObject(
                    COUNT_BOOK_TITLE_AUTHOR_QUERY,
                    of(
                            "title", title.trim().toLowerCase(),
                            "author", author.trim().toLowerCase()
                    ),
                    Integer.class
            );
            return count != null && count > 0;
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            throw new ApiException("Błąd. Spróbuj ponownie.");
        }
    }

    @Override
    public Book create(Book book) {

        try {
            KeyHolder holder = new GeneratedKeyHolder();
            SqlParameterSource parameters = getBookParameter(book);
            jdbc.update(INSERT_BOOK_QUERY, parameters, holder, new String[]{"id"});
            book.setId(requireNonNull(holder.getKey()).longValue());
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            throw new ApiException("Błąd. Spróbuj ponownie.");
        }
        return book;
    }

    @Override
    public Collection<Book> findAll(int page, int pageSize) {
        try {

            return jdbc.query(SELECT_ALL_BOOKS_QUERY, getPaginationParameters(page, pageSize), new BookRowMapper());

        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            throw new ApiException("Błąd. Spróbuj ponownie.");
        }
    }

    @Override
    public int countAll() {
        try {
            Integer count = jdbc.queryForObject(COUNT_ALL_BOOKS_QUERY, Map.of(), Integer.class);
            return count != null ? count : 0;
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            throw new ApiException("Błąd. Spróbuj ponownie.");
        }
    }

    @Override
    public Book findById(Long id) {
        try {
            return jdbc.queryForObject(SELECT_BOOK_BY_ID_QUERY, of("id", id), new BookRowMapper());
        } catch (EmptyResultDataAccessException exception) {
            throw new BookNotFoundException("Nie znaleziono książki id: " + id);
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            throw new ApiException("Błąd. Spróbuj ponownie.");
        }
    }

    @Override
    public boolean delete(Long id) {
        try {
            int deletedRows = jdbc.update(DELETE_BOOK_QUERY, of("id", id));
            if (deletedRows == 0) {
                throw new BookNotFoundException("Nie znaleziono książki id: " + id);
            }
            return true;
        } catch (BookNotFoundException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            throw new ApiException("Błąd. Spróbuj ponownie.");
        }
    }

    @Override
    public Book update(Book book) {
        try {
            SqlParameterSource parameters = getBookUpdateParameters(book);

            int updatedRows = jdbc.update(UPDATE_BOOK_QUERY, parameters);

            if (updatedRows == 0) {
                throw new BookNotFoundException("Nie znaleziono książki id: " + book.getId());
            }

            return book;

        } catch (BookNotFoundException exception) {
            throw exception;

        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            throw new ApiException("Błąd. Spróbuj ponownie.");
        }
    }

    @Override
    public Book getBookByIsbn(String isbn) {
        try {
            return jdbc.queryForObject(SELECT_BOOK_BY_ISBN_QUERY, of("isbn", isbn), new BookRowMapper());
        } catch (EmptyResultDataAccessException exception) {
            throw new BookNotFoundException("Nie znaleziono książki o isbn: " + isbn);
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            throw new ApiException("Błąd. Spróbuj ponownie.");
        }
    }

    @Override
    public Collection<Book> getBooksByTitle(String title, int page, int pageSize) {
        try {
            SqlParameterSource params = getPaginationParameters(page, pageSize)
                    .addValue("title", title.trim().toLowerCase());
            return jdbc.query(SELECT_BOOKS_BY_TITLE_QUERY, params, new BookRowMapper());

        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            throw new ApiException("Błąd. Spróbuj ponownie.");
        }
    }

    @Override
    public Collection<Book> getBooksByAuthor(String author, int page, int pageSize) {
        try {
            SqlParameterSource params = getPaginationParameters(page, pageSize)
                    .addValue("author", author.trim().toLowerCase());
            return jdbc.query(SELECT_BOOKS_BY_AUTHOR_QUERY, params, new BookRowMapper());

        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            throw new ApiException("Błąd. Spróbuj ponownie.");
        }
    }

    @Override
    public Collection<Book> getBooksByStatus(BookStatus status, int page, int pageSize) {
        try {
            SqlParameterSource params = getPaginationParameters(page, pageSize)
                    .addValue("status", status.name());
            return jdbc.query(SELECT_BOOKS_BY_STATUS_QUERY, params, new BookRowMapper());

        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            throw new ApiException("Błąd. Spróbuj ponownie.");
        }
    }

    private SqlParameterSource getBookParameter(Book book) {
        return new MapSqlParameterSource()
                .addValue("title", book.getTitle())
                .addValue("author", book.getAuthor())
                .addValue("isbn", book.getIsbn())
                .addValue("status", book.getStatus().name())
                .addValue("startDate", book.getStartDate())
                .addValue("finishDate", book.getFinishDate())
                .addValue("notes", book.getNotes());
    }

    private SqlParameterSource getBookUpdateParameters(Book book) {
        return new MapSqlParameterSource()
                .addValue("id", book.getId())
                .addValue("title", book.getTitle())
                .addValue("author", book.getAuthor())
                .addValue("isbn", book.getIsbn())
                .addValue("status", book.getStatus().name())
                .addValue("startDate", book.getStartDate())
                .addValue("finishDate", book.getFinishDate())
                .addValue("notes", book.getNotes());
    }

    private MapSqlParameterSource getPaginationParameters(int page, int pageSize) {
        return new MapSqlParameterSource()
                .addValue("pageSize", pageSize)
                .addValue("offset", page * pageSize);
    }
}
