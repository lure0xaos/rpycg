package gargoyle.rpycg.ex;

public class AppException extends RuntimeException {
    private static final long serialVersionUID = -1035913638893456880L;

    public AppException(final String message) {
        super(message);
    }

    public AppException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
