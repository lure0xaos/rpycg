package gargoyle.rpycg.ex;

public final class MalformedScriptException extends AppException {
    private static final long serialVersionUID = 6683531537857014068L;

    public MalformedScriptException(final String message) {
        super(message);
    }

    public MalformedScriptException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
