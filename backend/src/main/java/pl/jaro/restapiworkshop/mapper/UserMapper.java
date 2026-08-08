package pl.jaro.restapiworkshop.mapper;

import pl.jaro.restapiworkshop.dto.RegisterResponse;
import pl.jaro.restapiworkshop.model.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static RegisterResponse fromUser(User user) {
        return new RegisterResponse(
                user.getId(),
                user.getDisplayName(),
                user.getEmail()
        );
    }
}