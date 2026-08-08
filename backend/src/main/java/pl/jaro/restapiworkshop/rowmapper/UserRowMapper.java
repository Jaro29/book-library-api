package pl.jaro.restapiworkshop.rowmapper;

import org.springframework.jdbc.core.RowMapper;
import pl.jaro.restapiworkshop.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return User.builder()
                .id(resultSet.getLong("id"))
                .displayName(resultSet.getString("display_name"))
                .email(resultSet.getString("email"))
                .password(resultSet.getString("password"))
                .build();
    }
}