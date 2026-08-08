package pl.jaro.restapiworkshop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Nazwa wyświetlana jest wymagana")
        String displayName,

        @NotBlank(message = "Adres e-mail jest wymagany")
        @Email(message = "Adres e-mail jest nieprawidłowy")
        String email,

        @NotBlank(message = "Hasło jest wymagane")
        @Size(min = 8, message = "Hasło musi mieć co najmniej 8 znaków")
        String password
) {
}