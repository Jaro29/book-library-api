package pl.jaro.restapiworkshop.service;

import pl.jaro.restapiworkshop.model.User;

public interface UserService {

    User registerUser(String displayName, String email, String password);

    User login(String email, String password, String clientIp);
}
