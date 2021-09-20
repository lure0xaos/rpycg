package gargoyle.rpycg.ex;

import gargoyle.rpycg.fx.FXUtil;

import java.util.Locale;

public final class AppUserException extends AppException {
    public static final String LC_ERROR_NO_RESOURCES = "error.no-resources";
    public static final String LC_ERROR_NO_VIEW = "error.no-view";

    private static final long serialVersionUID = 8392680771655936441L;

    public AppUserException(final String code, final String... args) {
        super(FXUtil.message(AppUserException.class, Locale.getDefault(), code, args));
    }

    public AppUserException(final Throwable cause, final String code, final String... args) {
        super(FXUtil.message(AppUserException.class, Locale.getDefault(), code, args), cause);
    }
}
