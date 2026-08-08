package pl.jaro.restapiworkshop.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.jaro.restapiworkshop.dto.LoginRequest;
import pl.jaro.restapiworkshop.dto.LoginResponse;
import pl.jaro.restapiworkshop.dto.RegisterRequest;
import pl.jaro.restapiworkshop.dto.RegisterResponse;
import pl.jaro.restapiworkshop.mapper.UserMapper;
import pl.jaro.restapiworkshop.model.User;
import pl.jaro.restapiworkshop.service.JwtService;
import pl.jaro.restapiworkshop.service.UserService;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody @Valid RegisterRequest request) {
        User user = userService.registerUser(request.displayName(), request.email(), request.password());
        RegisterResponse response = UserMapper.fromUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request){
        User user = userService.login(request.email(),request.password());
        LoginResponse response = new LoginResponse(jwtService.generateToken(user.getId()), user.getDisplayName());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}