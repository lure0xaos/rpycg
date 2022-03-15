package gargoyle.rpycg.ex;

public class AppException extends RuntimeException {
    private static final long serialVersionUID = -1035913638893456880L;

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}
