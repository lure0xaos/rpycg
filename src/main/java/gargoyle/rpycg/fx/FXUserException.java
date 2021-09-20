package gargoyle.rpycg.fx;

import java.util.Locale;

public final class FXUserException extends FXException {
    public static final String LC_ERROR_INITIALIZATION = "error.initialization";
    public static final String LC_ERROR_NO_RESOURCES = "error.no-resources";
    public static final String LC_ERROR_NO_VIEW = "error.no-view";

    private static final long serialVersionUID = -4527495138860925941L;

    public FXUserException(final String code, final String... args) {
        super(FXUtil.message(FXUserException.class, Locale.getDefault(), code, args));
    }

    public FXUserException(final Throwable cause, final String code, final String... args) {
        super(FXUtil.message(FXUserException.class, Locale.getDefault(), code, args), cause);
    }
}
