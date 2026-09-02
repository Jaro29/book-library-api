package pl.jaro.restapiworkshop.service.implementation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.jaro.restapiworkshop.exception.EmailAlreadyExistsException;
import pl.jaro.restapiworkshop.exception.InvalidCredentialsException;
import pl.jaro.restapiworkshop.exception.TooManyLoginAttemptsException;
import pl.jaro.restapiworkshop.model.User;
import pl.jaro.restapiworkshop.repository.UserRepository;
import pl.jaro.restapiworkshop.service.LoginRateLimiter;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final String EMAIL = "jaro@example.com";
    private static final String RAW_PASSWORD = "tajnehaslo123";
    private static final String HASHED_PASSWORD = "$2a$10$zahaszowane";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private LoginRateLimiter loginRateLimiter;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldRejectRegistrationWhenEmailAlreadyTaken() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
                () -> userService.registerUser("Jaro", EMAIL, RAW_PASSWORD));

        verify(userRepository, never()).create(any());
    }

    @Test
    void shouldNeverStoreRawPassword() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(userRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.registerUser("Jaro", EMAIL, RAW_PASSWORD);

        assertThat(created.getPassword()).isEqualTo(HASHED_PASSWORD);
        assertThat(created.getPassword()).isNotEqualTo(RAW_PASSWORD);
    }

    @Test
    void shouldRejectLoginWhenEmailDoesNotExist() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> userService.login(EMAIL, RAW_PASSWORD));

        verify(loginRateLimiter).recordFailure(EMAIL);
    }

    @Test
    void shouldRejectLoginWhenPasswordDoesNotMatch() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(sampleUser()));
        when(passwordEncoder.matches("zle-haslo", HASHED_PASSWORD)).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> userService.login(EMAIL, "zle-haslo"));

        verify(loginRateLimiter).recordFailure(EMAIL);
    }

    @Test
    void shouldReturnUserOnSuccessfulLogin() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(sampleUser()));
        when(passwordEncoder.matches(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(true);

        User loggedIn = userService.login(EMAIL, RAW_PASSWORD);

        assertThat(loggedIn.getEmail()).isEqualTo(EMAIL);
        assertThat(loggedIn.getDisplayName()).isEqualTo("Jaro");
        verify(loginRateLimiter).reset(EMAIL);
        verify(loginRateLimiter, never()).recordFailure(EMAIL);
    }

    @Test
    void shouldNotEvenTouchTheDatabaseWhenAccountIsRateLimited() {
        doThrow(new TooManyLoginAttemptsException("Zbyt wiele prób."))
                .when(loginRateLimiter).checkNotBlocked(EMAIL);

        assertThrows(TooManyLoginAttemptsException.class,
                () -> userService.login(EMAIL, RAW_PASSWORD));

        verify(userRepository, never()).findByEmail(any());
    }

    private User sampleUser() {
        return User.builder()
                .id(1L)
                .displayName("Jaro")
                .email(EMAIL)
                .password(HASHED_PASSWORD)
                .build();
    }
}
