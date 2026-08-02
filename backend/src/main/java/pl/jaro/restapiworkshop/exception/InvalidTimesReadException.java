package pl.jaro.restapiworkshop.exception;

public class InvalidTimesReadException extends RuntimeException {
    public InvalidTimesReadException(String message) {
        super(message);
    }
}
