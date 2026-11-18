package pl.jaro.restapiworkshop.model;

/**
 * Dozwolone sortowania listy książek. ORDER BY nie przyjmuje parametru JDBC,
 * więc klient wybiera tylko nazwę z tej listy, a fragment SQL jest stały (bez SQL injection).
 * Każde sortowanie kończy się po id, żeby stronicowanie LIMIT/OFFSET było stabilne.
 */
public enum BookSort {
    ADDED("id DESC"),
    TITLE("title, id"),
    AUTHOR("author, id"),
    READ_DATE("finish_date IS NULL, finish_date DESC, id DESC");

    private final String orderBy;

    BookSort(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getOrderBy() {
        return orderBy;
    }
}
