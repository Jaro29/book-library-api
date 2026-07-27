package pl.jaro.restapiworkshop.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IsbnValidator implements ConstraintValidator<ValidIsbn, String> {
    @Override
    public boolean isValid(String isbn, ConstraintValidatorContext context) {

        // ISBN is optional - null or empty value is accepted
        if (isbn == null || isbn.isBlank()) {
            return true;
        }

        String normalized = isbn
                .replace("-", "")
                .replace(" ", "");

        return isValidIsbn10(normalized) || isValidIsbn13(normalized);
    }


    private boolean isValidIsbn10(String isbn) {

        if (!isbn.matches("\\d{9}[\\dXx]")) {
            return false;
        }

        int sum = 0;

        for (int i = 0; i < 10; i++) {
            char c = isbn.charAt(i);

            int value = (c == 'X' || c == 'x')
                    ? 10
                    : c - '0';

            sum += value * (10 - i);
        }

        return sum % 11 == 0;
    }


    private boolean isValidIsbn13(String isbn) {

        if (!isbn.matches("\\d{13}")) {
            return false;
        }

        int sum = 0;

        for (int i = 0; i < 13; i++) {
            int digit = isbn.charAt(i) - '0';

            sum += (i % 2 == 0)
                    ? digit
                    : digit * 3;
        }

        return sum % 10 == 0;
    }
}