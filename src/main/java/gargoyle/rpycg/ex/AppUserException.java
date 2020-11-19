package gargoyle.rpycg.ex;

import gargoyle.rpycg.fx.FXContextFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.ResourceBundle;

public final class AppUserException extends AppException {
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ex.AppUserException")
    public static final String LC_ERROR_NO_RESOURCES = "error.no-resources";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ex.AppUserException")
    public static final String LC_ERROR_NO_VIEW = "error.no-view";
    private static final String MSG_NO_STRING = "{} not found in resources of {}";
    private static final Logger log = LoggerFactory.getLogger(AppUserException.class);
    private static final ResourceBundle resources = FXContextFactory.currentContext()
            .loadResources(AppUserException.class)
            .orElseThrow(() -> new AppException(MessageFormat.format(LC_ERROR_NO_RESOURCES,
                    "gargoyle.rpycg.ex.AppUserException")));
    private static final long serialVersionUID = 8392680771655936441L;

    public AppUserException(@NotNull String code, @NotNull String... args) {
        super(message(code, args));
    }

    @NotNull
    private static String message(@NotNull @PropertyKey(resourceBundle = "gargoyle.rpycg.ex.AppUserException")
                                          String code,
                                  @NotNull String... args) {
        return Optional.ofNullable(resources)
                .filter(resourceBundle -> resourceBundle.containsKey(code))
                .map(resourceBundle -> resourceBundle.getString(code))
                .map(s -> MessageFormat.format(s, (Object[]) args))
                .orElseGet(() -> {
                    log.error(MSG_NO_STRING, code, AppUserException.class);
                    return MessageFormat.format(code, (Object[]) args);
                });
    }

    public AppUserException(@NotNull Throwable cause, @NotNull String code, @NotNull String... args) {
        super(message(code, args), cause);
    }
}
