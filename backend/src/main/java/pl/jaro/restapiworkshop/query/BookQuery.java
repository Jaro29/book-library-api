package pl.jaro.restapiworkshop.query;

public final class BookQuery {

    private BookQuery() {
    }

    public static final String INSERT_BOOK_QUERY = """
            INSERT INTO books (
                title,
                author,
                isbn,
                status,
                start_date,
                finish_date,
                times_read,
                notes,
                user_id
            )
            VALUES (
                :title,
                :author,
                :isbn,
                :status,
                :startDate,
                :finishDate,
                :timesRead,
                :notes,
                :userId
            )
            """;

    public static final String COUNT_BOOK_TITLE_AUTHOR_QUERY = """
            SELECT COUNT(*)
            FROM books
            WHERE LOWER(title) = :title
              AND LOWER(author) = :author
              AND user_id = :userId
            """;

    public static final String COUNT_BOOK_TITLE_AUTHOR_EXCLUDING_ID_QUERY = """
            SELECT COUNT(*)
            FROM books
            WHERE LOWER(title) = :title
              AND LOWER(author) = :author
              AND id != :id
              AND user_id = :userId
            """;

    public static final String SELECT_BOOK_BY_ID_QUERY =
            "SELECT * FROM books WHERE id = :id AND user_id = :userId";

    public static final String SELECT_BOOKS_BY_SEARCH_QUERY = """
            SELECT * FROM books
            WHERE (LOWER(title) LIKE :search OR LOWER(author) LIKE :search)
              AND user_id = :userId
            ORDER BY id
            LIMIT :pageSize OFFSET :offset
            """;

    public static final String COUNT_BOOKS_BY_SEARCH_QUERY = """
            SELECT COUNT(*)
            FROM books
            WHERE (LOWER(title) LIKE :search OR LOWER(author) LIKE :search)
              AND user_id = :userId
            """;

    public static final String UPDATE_BOOK_QUERY = """
            UPDATE books
            SET
                title = :title,
                author = :author,
                isbn = :isbn,
                status = :status,
                start_date = :startDate,
                finish_date = :finishDate,
                times_read = :timesRead,
                notes = :notes
            WHERE id = :id
              AND user_id = :userId
            """;

    public static final String SELECT_ALL_BOOKS_QUERY = """
            SELECT *
            FROM books
            WHERE user_id = :userId
            ORDER BY id
            LIMIT :pageSize OFFSET :offset
            """;

    public static final String COUNT_ALL_BOOKS_QUERY = """
            SELECT COUNT(*)
            FROM books
            WHERE user_id = :userId
            """;

    public static final String DELETE_BOOK_QUERY =
            "DELETE FROM books WHERE id = :id AND user_id = :userId";
}