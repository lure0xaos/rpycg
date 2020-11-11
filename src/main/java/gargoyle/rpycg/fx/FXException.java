package gargoyle.rpycg.fx;

import org.jetbrains.annotations.NotNull;

public class FXException extends RuntimeException {
    private static final long serialVersionUID = -5585188935029868045L;

    public FXException(@NotNull String message) {
        super(message);
    }

    public FXException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
    }
}
