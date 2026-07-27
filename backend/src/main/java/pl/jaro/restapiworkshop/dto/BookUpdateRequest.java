package pl.jaro.restapiworkshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pl.jaro.restapiworkshop.model.BookStatus;
import pl.jaro.restapiworkshop.validation.ValidIsbn;

import java.time.LocalDate;

public record BookUpdateRequest(
        @NotBlank(message = "Title cannot be blank")
        String title,
        @NotBlank(message = "Author cannot be blank")
        String author,
        @ValidIsbn
        String isbn,
        @NotNull(message = "Book status is required")
        BookStatus status,
        LocalDate startDate,
        LocalDate finishDate,

        String notes
) {
}
