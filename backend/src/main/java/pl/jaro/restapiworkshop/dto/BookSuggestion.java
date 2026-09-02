package pl.jaro.restapiworkshop.dto;

public record BookSuggestion(
        String title,
        String author,
        String isbn,
        String coverUrl,
        String publicationYear,
        String publisher
) {
}