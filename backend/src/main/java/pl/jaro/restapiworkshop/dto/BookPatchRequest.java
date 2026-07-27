package pl.jaro.restapiworkshop.dto;

import pl.jaro.restapiworkshop.model.BookStatus;
import pl.jaro.restapiworkshop.validation.ValidIsbn;

import java.time.LocalDate;

public record BookPatchRequest(
        String title,
        String author,

        @ValidIsbn
        String isbn,

        BookStatus status,
        LocalDate startDate,
        LocalDate finishDate,

        String notes
) {
}
