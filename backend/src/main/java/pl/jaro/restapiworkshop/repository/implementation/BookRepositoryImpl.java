package pl.jaro.restapiworkshop.repository.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import pl.jaro.restapiworkshop.exception.ApiException;
import pl.jaro.restapiworkshop.exception.BookNotFoundException;
import pl.jaro.restapiworkshop.exception.DuplicateBookException;
import pl.jaro.restapiworkshop.model.Book;
import pl.jaro.restapiworkshop.repository.BookRepository;
import pl.jaro.restapiworkshop.repository.BookSearchRepository;
import pl.jaro.restapiworkshop.rowmapper.BookRowMapper;

import java.util.Collection;
import java.util.function.Supplier;

import static java.util.Map.of;
import static java.util.Objects.requireNonNull;
import static pl.jaro.restapiworkshop.query.BookQuery.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class BookRepositoryImpl implements BookRepository, BookSearchRepository {

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public Book create(Book book) {
        return execute(() -> {
            KeyHolder holder = new GeneratedKeyHolder();
            SqlParameterSource parameters = getBookParameters(book);
            jdbc.update(INSERT_BOOK_QUERY, parameters, holder, new String[]{"id"});
            book.setId(requireNonNull(holder.getKey()).longValue());
            return book;
        });
    }

    @Override
    public boolean existsByTitleAndAuthor(String title, String author, Long userId) {
        return execute(() -> {
            Integer count = jdbc.queryForObject(
                    COUNT_BOOK_TITLE_AUTHOR_QUERY,
                    of("title", title.trim().toLowerCase(), "author", author.trim().toLowerCase(), "userId", userId),
                    Integer.class
            );
            return count != null && count > 0;
        });
    }

    @Override
    public boolean existsByTitleAndAuthorExcludingId(String title, String author, Long id, Long userId) {
        return execute(() -> {
            Integer count = jdbc.queryForObject(
                    COUNT_BOOK_TITLE_AUTHOR_EXCLUDING_ID_QUERY,
                    of("title", title.trim().toLowerCase(), "author", author.trim().toLowerCase(), "id", id, "userId", userId),
                    Integer.class
            );
            return count != null && count > 0;
        });
    }

    @Override
    public Collection<Book> findAll(int page, int pageSize, Long userId) {
        return execute(() -> jdbc.query(SELECT_ALL_BOOKS_QUERY,
                getPaginationParameters(page, pageSize).addValue("userId", userId), new BookRowMapper()));
    }

    @Override
    public long countAll(Long userId) {
        return execute(() -> {
            Long count = jdbc.queryForObject(COUNT_ALL_BOOKS_QUERY, of("userId", userId), Long.class);
            return count != null ? count : 0;
        });
    }

    @Override
    public Book findById(Long id, Long userId) {
        return execute(() -> {
            try {
                return jdbc.queryForObject(SELECT_BOOK_BY_ID_QUERY, of("id", id, "userId", userId), new BookRowMapper());
            } catch (EmptyResultDataAccessException exception) {
                throw new BookNotFoundException("Nie znaleziono książki id: " + id);
            }
        });
    }

    @Override
    public Book update(Book book) {
        return execute(() -> {
            SqlParameterSource parameters = getBookParameters(book);
            int updatedRows = jdbc.update(UPDATE_BOOK_QUERY, parameters);
            if (updatedRows == 0) {
                throw new BookNotFoundException("Nie znaleziono książki id: " + book.getId());
            }
            return book;
        });
    }

    @Override
    public void delete(Long id, Long userId) {
        execute(() -> {
            int deletedRows = jdbc.update(DELETE_BOOK_QUERY, of("id", id, "userId", userId));
            if (deletedRows == 0) {
                throw new BookNotFoundException("Nie znaleziono książki id: " + id);
            }
            return null;
        });
    }

    @Override
    public Collection<Book> searchBooks(String search, int page, int pageSize, Long userId) {
        return execute(() -> {
            String escaped = search.trim().toLowerCase()
                    .replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_");
            String searchParam = String.format("%%%s%%", escaped);
            SqlParameterSource params = getPaginationParameters(page, pageSize)
                    .addValue("search", searchParam)
                    .addValue("userId", userId);
            return jdbc.query(SELECT_BOOKS_BY_SEARCH_QUERY, params, new BookRowMapper());
        });
    }

    @Override
    public long countBySearch(String search, Long userId) {
        return execute(() -> {
            String escaped = search.trim().toLowerCase()
                    .replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_");
            String searchParam = String.format("%%%s%%", escaped);
            Long count = jdbc.queryForObject(COUNT_BOOKS_BY_SEARCH_QUERY,
                    of("search", searchParam, "userId", userId), Long.class);
            return count != null ? count : 0;
        });
    }

    private <T> T execute(Supplier<T> action) {
        try {
            return action.get();
        } catch (BookNotFoundException exception) {
            throw exception;
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateBookException("Książka o tym tytule, autorze lub ISBN już istnieje.");
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            throw new ApiException("Błąd. Spróbuj ponownie.");
        }
    }

    private SqlParameterSource getBookParameters(Book book) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("title", book.getTitle())
                .addValue("author", book.getAuthor())
                .addValue("isbn", book.getIsbn())
                .addValue("status", book.getStatus().name())
                .addValue("startDate", book.getStartDate())
                .addValue("finishDate", book.getFinishDate())
                .addValue("timesRead", book.getTimesRead())
                .addValue("notes", book.getNotes())
                .addValue("coverUrl", book.getCoverUrl())
                .addValue("userId", book.getUserId());

        if (book.getId() != null) {
            parameters.addValue("id", book.getId());
        }

        return parameters;
    }

    private MapSqlParameterSource getPaginationParameters(int page, int pageSize) {
        return new MapSqlParameterSource()
                .addValue("pageSize", pageSize)
                .addValue("offset", page * pageSize);
    }

}