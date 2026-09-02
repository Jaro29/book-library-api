package pl.jaro.restapiworkshop.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import pl.jaro.restapiworkshop.dto.BookSuggestion;
import tools.jackson.databind.JsonNode;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class BnDataService {

    private static final int FETCH_LIMIT = 100;
    private static final int MAX_RESULTS = 50;
    private static final Pattern YEAR_PATTERN = Pattern.compile("\\d{4}");

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
        Map<String, BookSuggestion> oldestByTitle = new LinkedHashMap<>();
        if (response == null || !response.has("bibs")) {
            return List.of();
        }

        for (JsonNode bib : response.get("bibs")) {
            JsonNode fields = bib.path("marc").path("fields");

            if (!"polski".equals(bib.path("language").asString(null))) {
                continue;
            }

            String mainAuthor = subfield(fields, "100", "a");
            if (!authorMatches(mainAuthor, requestedAuthor)) {
                continue;
            }

            String bookTitle = buildTitle(fields);
            if (bookTitle == null || bookTitle.isBlank()) {
                continue;
            }

            String isbn = subfield(fields, "020", "a");
            String publisher = cleanup(subfield(fields, "260", "b"));
            String publicationYear = bib.path("publicationYear").asString(null);

            BookSuggestion suggestion = new BookSuggestion(
                    bookTitle,
                    cleanup(mainAuthor),
                    isbn,
                    null,
                    publicationYear,
                    publisher
            );

            oldestByTitle.merge(bookTitle.toLowerCase(), suggestion, this::olderEdition);
        }

        return oldestByTitle.values().stream().limit(MAX_RESULTS).toList();
    }

    private BookSuggestion olderEdition(BookSuggestion current, BookSuggestion candidate) {
        Integer currentYear = extractYear(current.publicationYear());
        Integer candidateYear = extractYear(candidate.publicationYear());

        if (candidateYear == null) {
            return current;
        }
        if (currentYear == null || candidateYear < currentYear) {
            return candidate;
        }
        return current;
    }

    private Integer extractYear(String publicationYear) {
        if (publicationYear == null) {
            return null;
        }
        Matcher matcher = YEAR_PATTERN.matcher(publicationYear);
        return matcher.find() ? Integer.valueOf(matcher.group()) : null;
    }

    private String buildTitle(JsonNode fields) {
        String mainTitle = cleanup(subfield(fields, "245", "a"));
        String partNumber = cleanup(subfield(fields, "245", "n"));

        if (mainTitle == null || partNumber == null || partNumber.isBlank()) {
            return mainTitle;
        }
        return mainTitle + " " + partNumber;
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