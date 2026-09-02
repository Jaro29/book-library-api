package pl.jaro.restapiworkshop.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.jaro.restapiworkshop.exception.EmailAlreadyExistsException;
import pl.jaro.restapiworkshop.exception.InvalidCredentialsException;
import pl.jaro.restapiworkshop.model.User;
import pl.jaro.restapiworkshop.repository.UserRepository;
import pl.jaro.restapiworkshop.service.LoginRateLimiter;
import pl.jaro.restapiworkshop.service.UserService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginRateLimiter loginRateLimiter;

    @Override
    public User registerUser(String displayName, String email, String password) {
        boolean exists = userRepository.existsByEmail(email);
        if (exists) {
            throw new EmailAlreadyExistsException("Ten adres e-mail jest już używany. Użyj innego adresu e-mail i spróbuj ponownie.");
        }
        String encode = passwordEncoder.encode(password);
        User user = User.builder()
                .displayName(displayName)
                .email(email)
                .password(encode)
                .build();
        return userRepository.create(user);
    }

    @Override
    public User login(String email, String password) {
        loginRateLimiter.checkNotBlocked(email);

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            loginRateLimiter.recordFailure(email);
            throw new InvalidCredentialsException("Nieprawidłowy email lub hasło.");
        }

        User user = userOptional.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            loginRateLimiter.recordFailure(email);
            throw new InvalidCredentialsException("Nieprawidłowy email lub hasło.");
        }

        loginRateLimiter.reset(email);
        return user;
    }
}
