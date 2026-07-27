package pl.jaro.restapiworkshop.repository.implementation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import pl.jaro.restapiworkshop.exception.BookNotFoundException;
import pl.jaro.restapiworkshop.model.Book;
import pl.jaro.restapiworkshop.model.BookStatus;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@Import(BookRepositoryImpl.class)
class BookRepositoryImplTest {

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

        Book created = bookRepository.create(book);

        assertThat(created.getId()).isNotNull();
    }

    @Test
    void shouldFindBookById() {
        Book saved = bookRepository.create(sampleBook("Wiedźmin2"));

        Book found = bookRepository.findById(saved.getId());

        assertThat(found.getTitle()).isEqualTo("Wiedźmin2");
    }

    @Test
    void shouldThrowWhenBookNotFound() {
        assertThrows(BookNotFoundException.class, () -> bookRepository.findById(999L));
    }

    @Test
    void shouldFindBooksByTitleCaseInsensitive() {
        bookRepository.create(sampleBook("Wiedźmin3"));

        Collection<Book> results = bookRepository.getBooksByTitle("wiedźmin3", 0, 10);

        assertThat(results).hasSize(1);
    }

    @Test
    void shouldDetectDuplicateTitleAndAuthor() {
        Book book = sampleBook("Wiedźmin");
        bookRepository.create(book);

        boolean exists = bookRepository.existsByTitleAndAuthor("Wiedźmin", book.getAuthor());

        assertThat(exists).isTrue();
    }

    @Test
    void shouldDeleteBook() {
        Book saved = bookRepository.create(sampleBook("Do usunięcia"));
        bookRepository.delete(saved.getId());
        assertThrows(BookNotFoundException.class, () -> bookRepository.findById(saved.getId()));
    }

    private Book sampleBook(String title) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor("Andrzej Sapkowski");
        book.setIsbn("978" + (1000000000L + (long)(Math.random() * 9000000000L)));
        book.setStatus(BookStatus.TO_READ);
        return book;
    }
}