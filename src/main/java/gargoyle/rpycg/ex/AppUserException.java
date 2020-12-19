package gargoyle.rpycg.ex;

import gargoyle.rpycg.fx.FXContextFactory;
import gargoyle.rpycg.fx.Logger;
import gargoyle.rpycg.fx.LoggerFactory;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.ResourceBundle;

public final class AppUserException extends AppException {
    public static final String LC_ERROR_NO_RESOURCES = "error.no-resources";
    public static final String LC_ERROR_NO_VIEW = "error.no-view";
    private static final String MSG_NO_STRING = "{} not found in resources of {}";
    private static final Logger log = LoggerFactory.getLogger(AppUserException.class);
    private static final ResourceBundle resources = FXContextFactory.currentContext()
            .loadResources(AppUserException.class)
            .orElseThrow(() -> new AppException(MessageFormat.format(LC_ERROR_NO_RESOURCES,
                    "gargoyle.rpycg.ex.AppUserException")));
    private static final long serialVersionUID = 8392680771655936441L;

    public AppUserException(String code, String... args) {
        super(message(code, args));
    }

    private static String message(
            String code,
            String... args) {
        return Optional.ofNullable(resources)
                .filter(resourceBundle -> resourceBundle.containsKey(code))
                .map(resourceBundle -> resourceBundle.getString(code))
                .map(s -> MessageFormat.format(s, (Object[]) args))
                .orElseGet(() -> {
                    log.error(MSG_NO_STRING, code, AppUserException.class);
                    return MessageFormat.format(code, (Object[]) args);
                });
    }

    public AppUserException(Throwable cause, String code, String... args) {
        super(message(code, args), cause);
    }
}
