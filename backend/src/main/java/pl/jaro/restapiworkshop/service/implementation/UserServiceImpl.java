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
        String normalizedEmail = normalizeEmail(email);

        boolean exists = userRepository.existsByEmail(normalizedEmail);
        if (exists) {
            throw new EmailAlreadyExistsException("Ten adres e-mail jest już używany. Użyj innego adresu e-mail i spróbuj ponownie.");
        }
        String encode = passwordEncoder.encode(password);
        User user = User.builder()
                .displayName(displayName)
                .email(normalizedEmail)
                .password(encode)
                .build();
        return userRepository.create(user);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    @Override
    public User login(String email, String password, String clientIp) {
        String normalizedEmail = normalizeEmail(email);
        loginRateLimiter.checkNotBlocked(normalizedEmail, clientIp);

        Optional<User> userOptional = userRepository.findByEmail(normalizedEmail);
        if (userOptional.isEmpty()) {
            loginRateLimiter.recordFailure(normalizedEmail, clientIp);
            throw new InvalidCredentialsException("Nieprawidłowy email lub hasło.");
        }

        User user = userOptional.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            loginRateLimiter.recordFailure(normalizedEmail, clientIp);
            throw new InvalidCredentialsException("Nieprawidłowy email lub hasło.");
        }

        loginRateLimiter.reset(normalizedEmail, clientIp);
        return user;
    }
}
