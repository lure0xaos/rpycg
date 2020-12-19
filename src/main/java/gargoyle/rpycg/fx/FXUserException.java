package gargoyle.rpycg.fx;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.ResourceBundle;

public final class FXUserException extends FXException {
    public static final String LC_ERROR_INITIALIZATION = "error.initialization";
    public static final String LC_ERROR_NO_RESOURCES = "error.no-resources";
    public static final String LC_ERROR_NO_VIEW = "error.no-view";
    private static final String MSG_NO_STRING = "{} not found in resources of {}";
    private static final Logger log = LoggerFactory.getLogger(FXUserException.class);
    private static final ResourceBundle resources = FXContextFactory.currentContext()
            .loadResources(FXUserException.class)
            .orElseThrow(() -> new FXException(MessageFormat.format(LC_ERROR_NO_RESOURCES,
                    "gargoyle.rpycg.fx.FXUserException")));
    private static final long serialVersionUID = -4527495138860925941L;

    public FXUserException(String code, String... args) {
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
                    log.error(MSG_NO_STRING, code, FXUserException.class);
                    return MessageFormat.format(code, (Object[]) args);
                });
    }

    public FXUserException(Throwable cause, String code, String... args) {
        super(message(code, args), cause);
    }
}
