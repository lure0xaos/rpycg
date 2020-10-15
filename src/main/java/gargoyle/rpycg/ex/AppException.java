package gargoyle.rpycg.ex;

import org.jetbrains.annotations.NotNull;

public class AppException extends RuntimeException {
    private static final long serialVersionUID = -1035913638893456880L;

    public AppException(@NotNull String message) {
        super(message);
    }

    public AppException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
    }
}
