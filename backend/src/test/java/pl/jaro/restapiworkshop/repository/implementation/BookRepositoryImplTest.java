package pl.jaro.restapiworkshop.repository.implementation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import pl.jaro.restapiworkshop.exception.BookNotFoundException;
import pl.jaro.restapiworkshop.model.Book;
import pl.jaro.restapiworkshop.model.BookStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(BookRepositoryImpl.class)
class BookRepositoryImplTest {

    private static final Long USER_ID = 1L;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private BookRepositoryImpl bookRepository;

    @BeforeEach
    void setUp() {
        bookRepository = new BookRepositoryImpl(jdbcTemplate);
    }

    @Test
    void shouldCreateBookAndAssignId() {
        Book book = new Book();
        book.setTitle("Wiedźmin");
        book.setAuthor("Andrzej Sapkowski");
        book.setIsbn("9788328917545");
        book.setStatus(BookStatus.TO_READ);
        book.setUserId(USER_ID);

        Book created = bookRepository.create(book);

        assertThat(created.getId()).isNotNull();
    }

    @Test
    void shouldFindBookById() {
        Book saved = bookRepository.create(sampleBook("Wiedźmin2"));

        Book found = bookRepository.findById(saved.getId(), USER_ID);

        assertThat(found.getTitle()).isEqualTo("Wiedźmin2");
    }

    @Test
    void shouldThrowWhenBookNotFound() {
        assertThrows(BookNotFoundException.class, () -> bookRepository.findById(999L, USER_ID));
    }

    @Test
    void shouldDetectDuplicateTitleAndAuthor() {
        Book book = sampleBook("Wiedźmin");
        bookRepository.create(book);

        boolean exists = bookRepository.existsByTitleAndAuthor("Wiedźmin", book.getAuthor(), USER_ID);

        assertThat(exists).isTrue();
    }

    @Test
    void shouldDeleteBook() {
        Book saved = bookRepository.create(sampleBook("Do usunięcia"));
        bookRepository.delete(saved.getId(), USER_ID);
        assertThrows(BookNotFoundException.class, () -> bookRepository.findById(saved.getId(), USER_ID));
    }

    private Book sampleBook(String title) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor("Andrzej Sapkowski");
        book.setIsbn("978" + (1000000000L + (long) (Math.random() * 9000000000L)));
        book.setStatus(BookStatus.TO_READ);
        book.setUserId(USER_ID);
        return book;
    }

}