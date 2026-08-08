package pl.jaro.restapiworkshop.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Adres e-mail jest wymagany")
        String email,

        @NotBlank(message = "Hasło jest wymagane")
        String password
) {
}