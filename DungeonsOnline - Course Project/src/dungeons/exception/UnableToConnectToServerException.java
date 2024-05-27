package dungeons.exception;

public class UnableToConnectToServerException extends Exception {

    public UnableToConnectToServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnableToConnectToServerException(String message) {
        super(message);
    }

    public UnableToConnectToServerException(Throwable cause) {
        super(cause);
    }
}
