package pl.jaro.restapiworkshop.service;

import org.springframework.stereotype.Component;
import pl.jaro.restapiworkshop.exception.TooManyLoginAttemptsException;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final int MAX_TRACKED_ACCOUNTS = 10_000;

    private record FailedAttempts(int count, Instant firstAttempt) {
    }

    private final Map<String, FailedAttempts> failuresByEmail = new ConcurrentHashMap<>();

    public void checkNotBlocked(String email) {
        String key = key(email);
        FailedAttempts attempts = failuresByEmail.get(key);

        if (attempts == null) {
            return;
        }
        if (isExpired(attempts)) {
            failuresByEmail.remove(key);
            return;
        }
        if (attempts.count() >= MAX_ATTEMPTS) {
            throw new TooManyLoginAttemptsException(
                    "Zbyt wiele nieudanych prób logowania. Spróbuj ponownie za kilkanaście minut.");
        }
    }

    public void recordFailure(String email) {
        purgeIfTooLarge();
        failuresByEmail.compute(key(email), (ignored, existing) ->
                (existing == null || isExpired(existing))
                        ? new FailedAttempts(1, Instant.now())
                        : new FailedAttempts(existing.count() + 1, existing.firstAttempt()));
    }

    public void reset(String email) {
        failuresByEmail.remove(key(email));
    }

    private boolean isExpired(FailedAttempts attempts) {
        return attempts.firstAttempt().plus(WINDOW).isBefore(Instant.now());
    }

    private void purgeIfTooLarge() {
        if (failuresByEmail.size() >= MAX_TRACKED_ACCOUNTS) {
            failuresByEmail.values().removeIf(this::isExpired);
        }
    }

    private String key(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
