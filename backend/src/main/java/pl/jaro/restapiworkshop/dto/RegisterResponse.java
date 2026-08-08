package pl.jaro.restapiworkshop.dto;

public record RegisterResponse(
        Long id,
        String displayName,
        String email
) {
}