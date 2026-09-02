package pl.jaro.restapiworkshop.service;

import tools.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import pl.jaro.restapiworkshop.dto.BookSuggestion;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class GoogleBooksService {

    private final RestClient restClient;

    private final String apiKey;

    public GoogleBooksService(@Value("${app.google-books.api-key}") String apiKey) {
        this.apiKey = apiKey;
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(3));

        this.restClient = RestClient.builder()
                .baseUrl("https://www.googleapis.com/books/v1/volumes")
                .requestFactory(requestFactory)
                .build();
    }

    public List<BookSuggestion> search(String title, String author, String lang) {
        String query = buildQuery(title, author);

        try {
            JsonNode response = restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.queryParam("q", query).queryParam("maxResults", 20);
                        if (lang != null && !lang.isBlank()) {
                            uriBuilder.queryParam("langRestrict", lang);
                        }
                        if (apiKey != null && !apiKey.isBlank()) {
                            uriBuilder.queryParam("key", apiKey);
                        }
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .body(JsonNode.class);

            return mapToSuggestions(response, lang);
        } catch (Exception exception) {
            log.warn("Nie udało się pobrać wyników z Google Books: {}", exception.getMessage());
            return List.of();
        }
    }

    private String buildQuery(String title, String author) {
        StringBuilder query = new StringBuilder();
        if (title != null && !title.isBlank()) {
            query.append("intitle:").append(title.trim());
        }
        if (author != null && !author.isBlank()) {
            if (!query.isEmpty()) {
                query.append(" ");
            }
            query.append("inauthor:").append(author.trim());
        }
        return query.toString();
    }

    private List<BookSuggestion> mapToSuggestions(JsonNode response, String lang) {
        List<BookSuggestion> suggestions = new ArrayList<>();
        if (response == null || !response.has("items")) {
            return suggestions;
        }

        for (JsonNode item : response.get("items")) {
            JsonNode volumeInfo = item.path("volumeInfo");

            if (lang != null && !lang.isBlank()) {
                String itemLanguage = volumeInfo.path("language").asText(null);
                if (!lang.equals(itemLanguage)) {
                    continue;
                }
            }

            String bookTitle = volumeInfo.path("title").asText(null);
            if (bookTitle == null) {
                continue;
            }

            JsonNode authors = volumeInfo.path("authors");
            String bookAuthor = authors.isArray() && !authors.isEmpty() ? authors.get(0).asText() : null;

            String coverUrl = volumeInfo.path("imageLinks").path("thumbnail").asText(null);
            String isbn = extractIsbn(volumeInfo.path("industryIdentifiers"));

            suggestions.add(new BookSuggestion(bookTitle, bookAuthor, isbn, coverUrl, null, null));
        }

        return suggestions;
    }

    private String extractIsbn(JsonNode industryIdentifiers) {
        if (!industryIdentifiers.isArray()) {
            return null;
        }
        for (JsonNode identifier : industryIdentifiers) {
            if ("ISBN_13".equals(identifier.path("type").asText())) {
                return identifier.path("identifier").asText();
            }
        }
        for (JsonNode identifier : industryIdentifiers) {
            if ("ISBN_10".equals(identifier.path("type").asText())) {
                return identifier.path("identifier").asText();
            }
        }
        return null;
    }
}