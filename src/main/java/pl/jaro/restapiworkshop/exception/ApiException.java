package pl.jaro.restapiworkshop.exception;

public class ApiException extends RuntimeException{
    public ApiException(String message){
        super(message);
    }
}
