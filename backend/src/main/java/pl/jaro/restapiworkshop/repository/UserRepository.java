package pl.jaro.restapiworkshop.repository;

import pl.jaro.restapiworkshop.model.User;

import java.util.Optional;

public interface UserRepository {

    boolean existsByEmail(String email);

    User create(User user);

    Optional<User> findByEmail(String email);

}
