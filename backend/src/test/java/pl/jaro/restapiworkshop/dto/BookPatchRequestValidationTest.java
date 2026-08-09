package pl.jaro.restapiworkshop.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BookPatchRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldRejectPatchWithBlankTitle() {
        BookPatchRequest request = new BookPatchRequest(
                "   ", null, null, null, null, null, null, null, null
        );

        Set<ConstraintViolation<BookPatchRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Tytuł nie może być pusty");
    }

    @Test
    void shouldAcceptPatchWithNullTitle() {
        BookPatchRequest request = new BookPatchRequest(
                null, "Nowy Autor", null, null, null, null, null, null, null
        );

        Set<ConstraintViolation<BookPatchRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}
