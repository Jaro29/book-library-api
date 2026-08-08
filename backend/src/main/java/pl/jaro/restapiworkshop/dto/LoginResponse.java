package pl.jaro.restapiworkshop.dto;

public record LoginResponse(
        String token,
        String displayName
) {
}