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

import java.util.Collection;

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

    @Test
    void shouldUpdateBook() {
        Book saved = bookRepository.create(sampleBook("Przed zmianą"));

        saved.setTitle("Po zmianie");
        saved.setNotes("notatka");
        bookRepository.update(saved);

        Book updated = bookRepository.findById(saved.getId(), USER_ID);
        assertThat(updated.getTitle()).isEqualTo("Po zmianie");
        assertThat(updated.getNotes()).isEqualTo("notatka");
    }

    @Test
    void shouldNotUpdateBookOfAnotherUser() {
        Book saved = bookRepository.create(sampleBook("Cudza książka"));
        saved.setUserId(999L);
        saved.setTitle("Przejęta");

        assertThrows(BookNotFoundException.class, () -> bookRepository.update(saved));
    }

    @Test
    void shouldFindBookByTitleFragment() {
        bookRepository.create(sampleBook("Zzyzx Kwikwidacja"));

        Collection<Book> found = bookRepository.searchBooks("kwikwid", 0, 20, USER_ID);

        assertThat(found).hasSize(1);
        assertThat(found.iterator().next().getTitle()).isEqualTo("Zzyzx Kwikwidacja");
    }

    @Test
    void shouldFindBookByAuthorFragment() {
        Book book = sampleBook("Zzyzx Autorska");
        book.setAuthor("Bardzozacnyautor Testowy");
        bookRepository.create(book);

        Collection<Book> found = bookRepository.searchBooks("zacnyaut", 0, 20, USER_ID);

        assertThat(found).hasSize(1);
    }

    @Test
    void shouldIgnoreCaseWhenSearching() {
        bookRepository.create(sampleBook("Zzyzx Wielkoliterowa"));

        Collection<Book> found = bookRepository.searchBooks("WIELKOLITEROWA", 0, 20, USER_ID);

        assertThat(found).hasSize(1);
    }

    @Test
    void shouldReturnEmptyWhenNothingMatches() {
        Collection<Book> found = bookRepository.searchBooks("qqxzwv-nie-ma-takiej", 0, 20, USER_ID);

        assertThat(found).isEmpty();
    }

    @Test
    void shouldTreatWildcardCharactersAsLiteralText() {
        bookRepository.create(sampleBook("Zzyzx Zwykły Tytuł"));

        Collection<Book> foundByPercent = bookRepository.searchBooks("%", 0, 20, USER_ID);
        Collection<Book> foundByUnderscore = bookRepository.searchBooks("_", 0, 20, USER_ID);

        assertThat(foundByPercent).noneMatch(book -> "Zzyzx Zwykły Tytuł".equals(book.getTitle()));
        assertThat(foundByUnderscore).noneMatch(book -> "Zzyzx Zwykły Tytuł".equals(book.getTitle()));
    }

    @Test
    void shouldCountTheSameBooksThatSearchReturns() {
        bookRepository.create(sampleBook("Zzyzx Policzalna Pierwsza"));
        bookRepository.create(sampleBook("Zzyzx Policzalna Druga"));

        Collection<Book> found = bookRepository.searchBooks("zzyzx policzalna", 0, 20, USER_ID);
        long count = bookRepository.countBySearch("zzyzx policzalna", USER_ID);

        assertThat(found).hasSize(2);
        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldNotFindBooksOfAnotherUser() {
        bookRepository.create(sampleBook("Zzyzx Cudza Wyszukiwana"));

        Collection<Book> found = bookRepository.searchBooks("cudza wyszukiwana", 0, 20, 999L);

        assertThat(found).isEmpty();
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