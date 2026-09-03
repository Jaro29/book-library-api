package pl.jaro.restapiworkshop.service;

import org.springframework.stereotype.Component;
import pl.jaro.restapiworkshop.exception.TooManyLoginAttemptsException;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS_PER_SOURCE = 5;
    private static final int MAX_ATTEMPTS_PER_ACCOUNT = 20;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final int MAX_TRACKED_KEYS = 10_000;
    private static final String BLOCKED_MESSAGE =
            "Zbyt wiele nieudanych prób logowania. Spróbuj ponownie za kilkanaście minut.";

    private record FailedAttempts(int count, Instant firstAttempt) {
    }

    private final Map<String, FailedAttempts> failures = boundedMap();

    public void checkNotBlocked(String email, String clientIp) {
        checkKey(sourceKey(email, clientIp), MAX_ATTEMPTS_PER_SOURCE);
        checkKey(accountKey(email), MAX_ATTEMPTS_PER_ACCOUNT);
    }

    public void recordFailure(String email, String clientIp) {
        increment(sourceKey(email, clientIp));
        increment(accountKey(email));
    }

    public void reset(String email, String clientIp) {
        synchronized (failures) {
            failures.remove(sourceKey(email, clientIp));
            failures.remove(accountKey(email));
        }
    }

    private void checkKey(String key, int maxAttempts) {
        FailedAttempts attempts;
        synchronized (failures) {
            attempts = failures.get(key);
            if (attempts != null && isExpired(attempts)) {
                failures.remove(key);
                return;
            }
        }
        if (attempts != null && attempts.count() >= maxAttempts) {
            throw new TooManyLoginAttemptsException(BLOCKED_MESSAGE);
        }
    }

    private void increment(String key) {
        synchronized (failures) {
            FailedAttempts existing = failures.get(key);
            failures.put(key, (existing == null || isExpired(existing))
                    ? new FailedAttempts(1, Instant.now())
                    : new FailedAttempts(existing.count() + 1, existing.firstAttempt()));
        }
    }

    private boolean isExpired(FailedAttempts attempts) {
        return attempts.firstAttempt().plus(WINDOW).isBefore(Instant.now());
    }

    private String sourceKey(String email, String clientIp) {
        return "src|" + normalize(email) + "|" + normalize(clientIp);
    }

    private String accountKey(String email) {
        return "acc|" + normalize(email);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private static Map<String, FailedAttempts> boundedMap() {
        return Collections.synchronizedMap(new LinkedHashMap<>(64, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, FailedAttempts> eldest) {
                return size() > MAX_TRACKED_KEYS;
            }
        });
    }
}
