package pl.jaro.restapiworkshop.dto;

import pl.jaro.restapiworkshop.model.BookStatus;

import java.time.LocalDate;

public record BookResponse(

        Long id,

        String title,
        String author,
        String isbn,

        BookStatus status,
        LocalDate startDate,
        LocalDate finishDate,

        int timesRead,
        String notes

) {
}