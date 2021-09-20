package gargoyle.rpycg.ex;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;

public final class CodeGenerationException extends AppException {
    @Serial
    private static final long serialVersionUID = -1;

    public CodeGenerationException(final String message) {
        super(message);
    }

    public CodeGenerationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    @Serial
    private void readObject(final ObjectInputStream s) {
        throw new IllegalStateException(CodeGenerationException.class.getName());
    }

    @Serial
    private void writeObject(final ObjectOutputStream s) {
        throw new IllegalStateException(CodeGenerationException.class.getName());
    }
}
