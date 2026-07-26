package pl.jaro.restapiworkshop.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IsbnValidatorTest {
    private final IsbnValidator validator = new IsbnValidator();

    @Test
    void shouldAcceptValidIsbn10() {
        assertTrue(validator.isValid("0306406152", null));
    }


    @Test
    void shouldRejectInvalidIsbn10() {
        assertFalse(validator.isValid("0306406153", null));
    }


    @Test
    void shouldAcceptValidIsbn13() {
        assertTrue(validator.isValid("9788324631766", null));
    }


    @Test
    void shouldRejectInvalidIsbn13() {
        assertFalse(validator.isValid("9788324631767", null));
    }


    @Test
    void shouldAcceptNullValueBecauseIsbnIsOptional() {
        assertTrue(validator.isValid(null, null));
    }


    @Test
    void shouldAcceptBlankValueBecauseIsbnIsOptional() {
        assertTrue(validator.isValid("   ", null));
    }


    @Test
    void shouldAcceptIsbnWithSeparators() {
        assertTrue(validator.isValid("978-83-246-3176-6", null));
    }


    @Test
    void shouldRejectRandomText() {
        assertFalse(validator.isValid("abcdef", null));
    }
}