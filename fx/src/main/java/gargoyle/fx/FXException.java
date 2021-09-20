package gargoyle.fx;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;

public class FXException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -1;

    public FXException(final String message) {
        super(message);
    }

    public FXException(final String message, final Throwable cause) {
        super(message, cause);
    }

    @Serial
    private void readObject(final ObjectInputStream s) {
        throw new IllegalStateException(FXException.class.getName());
    }

    @Serial
    private void writeObject(final ObjectOutputStream s) {
        throw new IllegalStateException(FXException.class.getName());
    }
}
