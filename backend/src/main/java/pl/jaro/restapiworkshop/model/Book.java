package pl.jaro.restapiworkshop.model;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    private Long id;

    private String title;
    private String author;
    private String isbn;

    private BookStatus status;
    private LocalDate startDate;
    private LocalDate finishDate;

    private int timesRead;
    private String notes;
    private String coverUrl;

    private Long userId;

}