package gargoyle.rpycg.ex;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;

public class AppException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -1;

    public AppException(final String message) {
        super(message);
    }

    public AppException(final String message, final Throwable cause) {
        super(message, cause);
    }

    @Serial
    private void readObject(final ObjectInputStream s) {
        throw new IllegalStateException(AppException.class.getName());
    }

    @Serial
    private void writeObject(final ObjectOutputStream s) {
        throw new IllegalStateException(AppException.class.getName());
    }
}
