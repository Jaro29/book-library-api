package pl.jaro.restapiworkshop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.jaro.restapiworkshop.exception.TooManyLoginAttemptsException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginRateLimiterTest {

    private static final String EMAIL = "jaro@example.com";
    private static final String IP = "10.0.0.1";
    private static final String OTHER_IP = "10.0.0.2";

    private LoginRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new LoginRateLimiter();
    }

    @Test
    void shouldAllowLoginWhenThereAreNoFailures() {
        assertDoesNotThrow(() -> rateLimiter.checkNotBlocked(EMAIL, IP));
    }

    @Test
    void shouldAllowLoginJustBelowTheLimit() {
        recordFailures(4, EMAIL, IP);

        assertDoesNotThrow(() -> rateLimiter.checkNotBlocked(EMAIL, IP));
    }

    @Test
    void shouldBlockLoginAfterTooManyFailuresFromTheSameSource() {
        recordFailures(5, EMAIL, IP);

        assertThrows(TooManyLoginAttemptsException.class, () -> rateLimiter.checkNotBlocked(EMAIL, IP));
    }

    @Test
    void shouldNotLockTheOwnerOutWhenSomeoneElseGuessesTheirPassword() {
        recordFailures(5, EMAIL, OTHER_IP);

        assertDoesNotThrow(() -> rateLimiter.checkNotBlocked(EMAIL, IP));
    }

    @Test
    void shouldBlockAccountWhenAttackIsSpreadAcrossManySources() {
        for (int i = 0; i < 20; i++) {
            rateLimiter.recordFailure(EMAIL, "10.0.0." + i);
        }

        assertThrows(TooManyLoginAttemptsException.class, () -> rateLimiter.checkNotBlocked(EMAIL, IP));
    }

    @Test
    void shouldUnblockAfterSuccessfulLogin() {
        recordFailures(5, EMAIL, IP);

        rateLimiter.reset(EMAIL, IP);

        assertDoesNotThrow(() -> rateLimiter.checkNotBlocked(EMAIL, IP));
    }

    @Test
    void shouldTrackAccountsSeparately() {
        recordFailures(5, EMAIL, IP);

        assertDoesNotThrow(() -> rateLimiter.checkNotBlocked("ktos.inny@example.com", IP));
    }

    @Test
    void shouldIgnoreCaseAndWhitespaceInEmail() {
        recordFailures(5, EMAIL, IP);

        assertThrows(TooManyLoginAttemptsException.class,
                () -> rateLimiter.checkNotBlocked("  JARO@Example.COM  ", IP));
    }

    @Test
    void shouldStayBoundedUnderADictionaryAttack() {
        for (int i = 0; i < 30_000; i++) {
            rateLimiter.recordFailure("ofiara" + i + "@example.com", IP);
        }

        assertDoesNotThrow(() -> rateLimiter.checkNotBlocked(EMAIL, IP));
    }

    private void recordFailures(int times, String email, String ip) {
        for (int i = 0; i < times; i++) {
            rateLimiter.recordFailure(email, ip);
        }
    }
}
