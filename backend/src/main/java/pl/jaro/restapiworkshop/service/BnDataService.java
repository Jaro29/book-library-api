package pl.jaro.restapiworkshop.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import pl.jaro.restapiworkshop.dto.BookSuggestion;
import tools.jackson.databind.JsonNode;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class BnDataService {

    private static final int FETCH_LIMIT = 50;
    private static final int MAX_RESULTS = 20;

    private final RestClient restClient;

    public BnDataService() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(3));

        this.restClient = RestClient.builder()
                .baseUrl("https://data.bn.org.pl/api/institutions/bibs.json")
                .requestFactory(requestFactory)
                .build();
    }

    public List<BookSuggestion> search(String title, String author) {
        try {
            JsonNode response = restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.queryParam("kind", "książka")
                                .queryParam("language", "polski")
                                .queryParam("limit", FETCH_LIMIT);
                        if (title != null && !title.isBlank()) {
                            uriBuilder.queryParam("title", title.trim());
                        }
                        if (author != null && !author.isBlank()) {
                            uriBuilder.queryParam("author", author.trim());
                        }
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .body(JsonNode.class);

            return mapToSuggestions(response, author);
        } catch (Exception exception) {
            log.warn("Nie udało się pobrać wyników z BN Data: {}", exception.getMessage());
            return List.of();
        }
    }

    private List<BookSuggestion> mapToSuggestions(JsonNode response, String requestedAuthor) {
        List<BookSuggestion> suggestions = new ArrayList<>();
        if (response == null || !response.has("bibs")) {
            return suggestions;
        }

        for (JsonNode bib : response.get("bibs")) {
            if (suggestions.size() >= MAX_RESULTS) {
                break;
            }

            JsonNode fields = bib.path("marc").path("fields");

            if (!"polski".equals(bib.path("language").asString(null))) {
                continue;
            }

            String mainAuthor = subfield(fields, "100", "a");
            if (!authorMatches(mainAuthor, requestedAuthor)) {
                continue;
            }

            String bookTitle = cleanup(subfield(fields, "245", "a"));
            if (bookTitle == null || bookTitle.isBlank()) {
                continue;
            }

            String isbn = subfield(fields, "020", "a");
            String publisher = cleanup(subfield(fields, "260", "b"));
            String publicationYear = bib.path("publicationYear").asString(null);

            suggestions.add(new BookSuggestion(
                    bookTitle,
                    cleanup(mainAuthor),
                    isbn,
                    null,
                    publicationYear,
                    publisher
            ));
        }

        return suggestions;
    }

    private boolean authorMatches(String mainAuthor, String requestedAuthor) {
        if (requestedAuthor == null || requestedAuthor.isBlank()) {
            return true;
        }
        if (mainAuthor == null) {
            return false;
        }

        String normalizedAuthor = mainAuthor.toLowerCase();
        for (String token : requestedAuthor.trim().toLowerCase().split("[\\s,]+")) {
            if (!token.isBlank() && !normalizedAuthor.contains(token)) {
                return false;
            }
        }
        return true;
    }

    private String subfield(JsonNode fields, String tag, String code) {
        if (!fields.isArray()) {
            return null;
        }
        for (JsonNode field : fields) {
            JsonNode value = field.path(tag);
            if (value.isMissingNode() || !value.has("subfields")) {
                continue;
            }
            for (JsonNode subfield : value.get("subfields")) {
                String text = subfield.path(code).asString(null);
                if (text != null) {
                    return text;
                }
            }
        }
        return null;
    }

    private String cleanup(String value) {
        if (value == null) {
            return null;
        }
        return value.trim()
                .replaceAll("[/;,:.]+$", "")
                .replace("\"", "")
                .trim();
    }
}