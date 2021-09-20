package gargoyle.rpycg.ex;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;

public final class MalformedScriptException extends AppException {
    @Serial
    private static final long serialVersionUID = -1;

    public MalformedScriptException(final String message) {
        super(message);
    }

    public MalformedScriptException(final String message, final Throwable cause) {
        super(message, cause);
    }

    @Serial
    private void readObject(final ObjectInputStream s) {
        throw new IllegalStateException(MalformedScriptException.class.getName());
    }

    @Serial
    private void writeObject(final ObjectOutputStream s) {
        throw new IllegalStateException(MalformedScriptException.class.getName());
    }
}
