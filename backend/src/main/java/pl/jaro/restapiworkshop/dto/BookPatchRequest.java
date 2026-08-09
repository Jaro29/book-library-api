package pl.jaro.restapiworkshop.dto;

import pl.jaro.restapiworkshop.model.BookStatus;
import pl.jaro.restapiworkshop.validation.NullOrNotBlank;
import pl.jaro.restapiworkshop.validation.ValidIsbn;

import java.time.LocalDate;

public record BookPatchRequest(
        @NullOrNotBlank(message = "Tytuł nie może być pusty")
        String title,

        @NullOrNotBlank(message = "Autor nie może być pusty")
        String author,

        @ValidIsbn
        String isbn,

        BookStatus status,
        LocalDate startDate,
        LocalDate finishDate,

        Integer timesRead,
        String notes
) {
}
