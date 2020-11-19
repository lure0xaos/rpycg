package gargoyle.rpycg.fx;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.ResourceBundle;

public final class FXUserException extends FXException {
    @PropertyKey(resourceBundle = "gargoyle.rpycg.fx.FXUserException")
    public static final String LC_ERROR_INITIALIZATION = "error.initialization";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.fx.FXUserException")
    public static final String LC_ERROR_NO_RESOURCES = "error.no-resources";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.fx.FXUserException")
    public static final String LC_ERROR_NO_VIEW = "error.no-view";
    private static final String MSG_NO_STRING = "{} not found in resources of {}";
    private static final Logger log = LoggerFactory.getLogger(FXUserException.class);
    private static final ResourceBundle resources = FXContextFactory.currentContext()
            .loadResources(FXUserException.class)
            .orElseThrow(() -> new FXException(MessageFormat.format(LC_ERROR_NO_RESOURCES,
                    "gargoyle.rpycg.fx.FXUserException")));
    private static final long serialVersionUID = -4527495138860925941L;

    public FXUserException(@NotNull String code, @NotNull String... args) {
        super(message(code, args));
    }

    @NotNull
    private static String message(@NotNull @PropertyKey(resourceBundle = "gargoyle.rpycg.fx.FXUserException")
                                          String code,
                                  @NotNull String... args) {
        return Optional.ofNullable(resources)
                .filter(resourceBundle -> resourceBundle.containsKey(code))
                .map(resourceBundle -> resourceBundle.getString(code))
                .map(s -> MessageFormat.format(s, (Object[]) args))
                .orElseGet(() -> {
                    log.error(MSG_NO_STRING, code, FXUserException.class);
                    return MessageFormat.format(code, (Object[]) args);
                });
    }

    public FXUserException(@NotNull Throwable cause, @NotNull String code, @NotNull String... args) {
        super(message(code, args), cause);
    }
}
