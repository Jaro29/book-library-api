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
                notes
            )
            VALUES (
                :title,
                :author,
                :isbn,
                :status,
                :startDate,
                :finishDate,
                :notes
            )
            """;

    public static final String COUNT_BOOK_TITLE_AUTHOR_QUERY = """
            SELECT COUNT(*)
            FROM books
            WHERE LOWER(title) = :title
              AND LOWER(author) = :author
            """;

    public static final String COUNT_BOOK_TITLE_AUTHOR_EXCLUDING_ID_QUERY = """
            SELECT COUNT(*)
            FROM books
            WHERE LOWER(title) = :title
              AND LOWER(author) = :author
              AND id != :id
            """;

    public static final String SELECT_BOOK_BY_ID_QUERY =
            "SELECT * FROM books WHERE id = :id";

    public static final String SELECT_BOOK_BY_ISBN_QUERY =
            "SELECT * FROM books WHERE isbn = :isbn";

    public static final String SELECT_BOOKS_BY_TITLE_QUERY = """
            SELECT * FROM books
            WHERE LOWER(title) = :title
            ORDER BY id
            LIMIT :pageSize OFFSET :offset
            """;

    public static final String SELECT_BOOKS_BY_AUTHOR_QUERY = """
            SELECT * FROM books
            WHERE LOWER(author) = :author
            ORDER BY id
            LIMIT :pageSize OFFSET :offset
            """;

    public static final String SELECT_BOOKS_BY_STATUS_QUERY = """
            SELECT * FROM books
            WHERE status = :status
            ORDER BY id
            LIMIT :pageSize OFFSET :offset
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
                notes = :notes
            WHERE id = :id
            """;

    public static final String SELECT_ALL_BOOKS_QUERY = """
            SELECT *
            FROM books
            ORDER BY id
            LIMIT :pageSize OFFSET :offset
            """;

    public static final String COUNT_ALL_BOOKS_QUERY = """
            SELECT COUNT(*)
            FROM books
            """;

    public static final String DELETE_BOOK_QUERY =
            "DELETE FROM books WHERE id = :id";
}
