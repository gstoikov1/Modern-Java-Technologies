package dungeons.exception;

public class PlayerCharAlreadyExistsException extends Exception {
    public PlayerCharAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public PlayerCharAlreadyExistsException(String message) {
        super(message);
    }

    public PlayerCharAlreadyExistsException(Throwable cause) {
        super(cause);
    }
}
