package gargoyle.rpycg.ex;

public class MalformedScriptException extends AppException {
    public MalformedScriptException(String message) {
        super(message);
    }

    public MalformedScriptException(String message, Throwable cause) {
        super(message, cause);
    }
}
