package gargoyle.rpycg.fx;

public class FXException extends RuntimeException {
    private static final long serialVersionUID = -5585188935029868045L;

    public FXException(final String message) {
        super(message);
    }

    public FXException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
