package gargoyle.fx;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.util.Map;

public final class FXUserException extends FXException {
    @Serial
    private static final long serialVersionUID = -1;

    public FXUserException(final String code, final Map<String, ?> args) {
        super(FXUtil.format(code, args));
    }

    public FXUserException(final Throwable cause, final String code, final Map<String, ?> args) {
        super(FXUtil.format(code, args), cause);
    }

    @Serial
    private void readObject(final ObjectInputStream s) {
        throw new IllegalStateException(FXUserException.class.getName());
    }

    @Serial
    private void writeObject(final ObjectOutputStream s) {
        throw new IllegalStateException(FXUserException.class.getName());
    }
}
