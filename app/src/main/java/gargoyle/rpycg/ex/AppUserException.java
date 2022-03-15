package gargoyle.rpycg.ex;

import gargoyle.fx.FXUtil;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.util.Map;

public final class AppUserException extends AppException {
    @Serial
    private static final long serialVersionUID = -1;

    public AppUserException(final String code, final Map<String, ?> args) {
        super(FXUtil.format(code, args));
    }

    public AppUserException(final Throwable cause, final String code, final Map<String, ?> args) {
        super(FXUtil.format(code, args), cause);
    }

    @Serial
    private void readObject(final ObjectInputStream s) {
        throw new IllegalStateException(AppUserException.class.getName());
    }

    @Serial
    private void writeObject(final ObjectOutputStream s) {
        throw new IllegalStateException(AppUserException.class.getName());
    }
}
