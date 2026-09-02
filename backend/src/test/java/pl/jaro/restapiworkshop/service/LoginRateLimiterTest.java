package pl.jaro.restapiworkshop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.jaro.restapiworkshop.exception.TooManyLoginAttemptsException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginRateLimiterTest {

    private static final String EMAIL = "jaro@example.com";

    private LoginRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new LoginRateLimiter();
    }

    @Test
    void shouldAllowLoginWhenThereAreNoFailures() {
        assertDoesNotThrow(() -> rateLimiter.checkNotBlocked(EMAIL));
    }

    @Test
    void shouldAllowLoginJustBelowTheLimit() {
        for (int i = 0; i < 4; i++) {
            rateLimiter.recordFailure(EMAIL);
        }

        assertDoesNotThrow(() -> rateLimiter.checkNotBlocked(EMAIL));
    }

    @Test
    void shouldBlockLoginAfterTooManyFailures() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.recordFailure(EMAIL);
        }

        assertThrows(TooManyLoginAttemptsException.class, () -> rateLimiter.checkNotBlocked(EMAIL));
    }

    @Test
    void shouldUnblockAfterSuccessfulLogin() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.recordFailure(EMAIL);
        }

        rateLimiter.reset(EMAIL);

        assertDoesNotThrow(() -> rateLimiter.checkNotBlocked(EMAIL));
    }

    @Test
    void shouldTrackAccountsSeparately() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.recordFailure(EMAIL);
        }

        assertDoesNotThrow(() -> rateLimiter.checkNotBlocked("ktos.inny@example.com"));
    }

    @Test
    void shouldIgnoreCaseAndWhitespaceInEmail() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.recordFailure(EMAIL);
        }

        assertThrows(TooManyLoginAttemptsException.class,
                () -> rateLimiter.checkNotBlocked("  JARO@Example.COM  "));
    }
}
