package gargoyle.rpycg.fx;

public class FXException extends RuntimeException {
    private static final long serialVersionUID = -5585188935029868045L;

    public FXException(String message) {
        super(message);
    }

    public FXException(String message, Throwable cause) {
        super(message, cause);
    }
}
