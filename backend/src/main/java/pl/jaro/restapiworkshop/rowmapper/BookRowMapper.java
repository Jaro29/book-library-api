package pl.jaro.restapiworkshop.rowmapper;

import pl.jaro.restapiworkshop.model.Book;
import pl.jaro.restapiworkshop.model.BookStatus;

import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BookRowMapper implements RowMapper<Book> {
    @Override
    public Book mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return Book.builder()
                .id(resultSet.getLong("id"))
                .title(resultSet.getString("title"))
                .author(resultSet.getString("author"))
                .isbn(resultSet.getString("isbn"))
                .status(BookStatus.valueOf(resultSet.getString("status")))
                .startDate(resultSet.getObject("start_date", java.time.LocalDate.class))
                .finishDate(resultSet.getObject("finish_date", java.time.LocalDate.class))
                .notes(resultSet.getString("notes"))
                .build();
    }
}
