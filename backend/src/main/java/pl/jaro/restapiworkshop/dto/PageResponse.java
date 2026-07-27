package pl.jaro.restapiworkshop.dto;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int pageSize,
        long totalElements,
        int totalPages
) {
}
