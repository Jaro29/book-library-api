package pl.jaro.restapiworkshop.mapper;

import pl.jaro.restapiworkshop.dto.BookCreateRequest;
import pl.jaro.restapiworkshop.dto.BookPatchRequest;
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
                .timesRead(createRequest.timesRead() != null ? createRequest.timesRead() : 0)
                .notes(createRequest.notes())
                .build();
    }

    public static Book toBook(BookPatchRequest patchRequest, Book book) {
        return Book.builder()
                .id(book.getId())
                .title(patchRequest.title() != null ? patchRequest.title() : book.getTitle())
                .author(patchRequest.author() != null ? patchRequest.author() : book.getAuthor())
                .isbn(patchRequest.isbn() != null ? patchRequest.isbn() : book.getIsbn())
                .status(patchRequest.status() != null ? patchRequest.status() : book.getStatus())
                .startDate(patchRequest.startDate() != null ? patchRequest.startDate() : book.getStartDate())
                .finishDate(patchRequest.finishDate() != null ? patchRequest.finishDate() : book.getFinishDate())
                .timesRead(patchRequest.timesRead() != null ? patchRequest.timesRead() : book.getTimesRead())
                .notes(patchRequest.notes() != null ? patchRequest.notes() : book.getNotes())
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
                book.getTimesRead(),
                book.getNotes()
        );
    }
}