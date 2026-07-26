package pl.jaro.restapiworkshop.mapper;

import pl.jaro.restapiworkshop.dto.BookCreateRequest;
import pl.jaro.restapiworkshop.dto.BookResponse;
import pl.jaro.restapiworkshop.model.Book;
import pl.jaro.restapiworkshop.model.BookStatus;

public final class BookMapper {

    private BookMapper() {
    }

    public static Book toBook(BookCreateRequest createRequest) {
        return Book.builder()
                .title(createRequest.title())
                .author(createRequest.author())
                .isbn(createRequest.isbn())
                .status(createRequest.status() != null ? createRequest.status() : BookStatus.TO_READ)
                .startDate(createRequest.startDate())
                .finishDate(createRequest.finishDate())
                .notes(createRequest.notes())
                .build();
    }

    public static BookResponse fromBook(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getStatus(),
                book.getStartDate(),
                book.getFinishDate(),
                book.getNotes()
        );
    }
}