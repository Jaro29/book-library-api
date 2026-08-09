package pl.jaro.restapiworkshop.dto;

import jakarta.validation.constraints.NotBlank;
import pl.jaro.restapiworkshop.model.BookStatus;
import pl.jaro.restapiworkshop.validation.ValidIsbn;

import java.time.LocalDate;

public record BookCreateRequest(

        @NotBlank(message = "Tytuł nie może być pusty")
        String title,
        @NotBlank(message = "Autor nie może być pusty")
        String author,
        @ValidIsbn
        String isbn,

        BookStatus status,
        LocalDate startDate,
        LocalDate finishDate,

        Integer timesRead,
        String notes,
        String coverUrl
) {
}
