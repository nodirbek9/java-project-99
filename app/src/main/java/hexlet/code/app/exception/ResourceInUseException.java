package hexlet.code.app.exception;

public class ResourceInUseException extends RuntimeException {

    public ResourceInUseException(String message) {
        super(message);
    }
}
