package pl.jaro.restapiworkshop.service;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String SECRET = "testowy-sekret-do-testow-musi-miec-co-najmniej-256-bitow-dlugosci";
    private static final String OTHER_SECRET = "zupelnie-inny-sekret-rowniez-odpowiednio-dlugi-do-podpisywania-hs";
    private static final long ONE_HOUR = 3_600_000L;

    @Test
    void shouldExtractTheSameUserIdThatWasPutInTheToken() {
        JwtService jwtService = new JwtService(SECRET, ONE_HOUR);

        String token = jwtService.generateToken(42L);

        assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
    }

    @Test
    void shouldRejectExpiredToken() {
        String expiredToken = new JwtService(SECRET, -1000L).generateToken(42L);

        JwtService jwtService = new JwtService(SECRET, ONE_HOUR);

        assertThrows(JwtException.class, () -> jwtService.extractUserId(expiredToken));
    }

    @Test
    void shouldRejectTokenSignedWithAnotherSecret() {
        String foreignToken = new JwtService(OTHER_SECRET, ONE_HOUR).generateToken(42L);

        JwtService jwtService = new JwtService(SECRET, ONE_HOUR);

        assertThrows(JwtException.class, () -> jwtService.extractUserId(foreignToken));
    }

    @Test
    void shouldRejectTamperedToken() {
        JwtService jwtService = new JwtService(SECRET, ONE_HOUR);
        String token = jwtService.generateToken(42L);
        String tampered = token.substring(0, token.length() - 4) + "AAAA";

        assertThrows(JwtException.class, () -> jwtService.extractUserId(tampered));
    }

    @Test
    void shouldRejectGarbageInsteadOfToken() {
        JwtService jwtService = new JwtService(SECRET, ONE_HOUR);

        assertThrows(JwtException.class, () -> jwtService.extractUserId("to-nie-jest-token"));
    }
}
