package pl.jaro.restapiworkshop.dto;

import jakarta.validation.constraints.NotBlank;
import pl.jaro.restapiworkshop.model.BookStatus;
import pl.jaro.restapiworkshop.validation.ValidIsbn;

import java.time.LocalDate;

public record BookCreateRequest(

        @NotBlank(message = "Title cannot be blank")
        String title,
        @NotBlank(message = "Author cannot be blank")
        String author,
        @ValidIsbn
        String isbn,

        BookStatus status,
        LocalDate startDate,
        LocalDate finishDate,

        String notes
) {
}
