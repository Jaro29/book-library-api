package pl.jaro.restapiworkshop.repository.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import pl.jaro.restapiworkshop.exception.ApiException;
import pl.jaro.restapiworkshop.exception.EmailAlreadyExistsException;
import pl.jaro.restapiworkshop.model.User;
import pl.jaro.restapiworkshop.repository.UserRepository;
import pl.jaro.restapiworkshop.rowmapper.UserRowMapper;

import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Map.of;
import static java.util.Objects.requireNonNull;
import static pl.jaro.restapiworkshop.query.UserQuery.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl implements UserRepository {

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public User create(User user) {
        return execute(() -> {
            KeyHolder holder = new GeneratedKeyHolder();
            SqlParameterSource parameters = new MapSqlParameterSource()
                    .addValue("displayName", user.getDisplayName())
                    .addValue("email", user.getEmail())
                    .addValue("password", user.getPassword());
            jdbc.update(INSERT_USER_QUERY, parameters, holder, new String[]{"id"});
            user.setId(requireNonNull(holder.getKey()).longValue());
            return user;
        });
    }

    @Override
    public boolean existsByEmail(String email) {
        return execute(() -> {
            Integer count = jdbc.queryForObject(
                    COUNT_USER_BY_EMAIL_QUERY,
                    of("email", email.trim().toLowerCase()),
                    Integer.class
            );
            return count != null && count > 0;
        });
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return execute(() -> {
            try {
                User user = jdbc.queryForObject(
                        SELECT_USER_BY_EMAIL_QUERY,
                        of("email", email.trim().toLowerCase()),
                        new UserRowMapper()
                );
                return Optional.ofNullable(user);
            } catch (EmptyResultDataAccessException exception) {
                return Optional.empty();
            }
        });
    }

    private <T> T execute(Supplier<T> action) {
        try {
            return action.get();
        } catch (DataIntegrityViolationException exception) {
            throw new EmailAlreadyExistsException("Ten adres e-mail jest już używany. Użyj innego adresu e-mail i spróbuj ponownie.");
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            throw new ApiException("Błąd. Spróbuj ponownie.");
        }
    }
}