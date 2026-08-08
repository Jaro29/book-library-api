package pl.jaro.restapiworkshop.query;

public final class UserQuery {

    private UserQuery() {
    }

    public static final String INSERT_USER_QUERY = """
            INSERT INTO users (
                display_name,
                email,
                password
            )
            VALUES (
                :displayName,
                :email,
                :password
            )
            """;

    public static final String COUNT_USER_BY_EMAIL_QUERY = """
            SELECT COUNT(*)
            FROM users
            WHERE LOWER(email) = :email
            """;

    public static final String SELECT_USER_BY_EMAIL_QUERY =
            "SELECT * FROM users WHERE LOWER(email) = :email";
}