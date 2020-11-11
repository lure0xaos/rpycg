package gargoyle.rpycg.util;

import gargoyle.rpycg.ex.AppException;
import gargoyle.rpycg.ex.AppUserException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import java.util.function.Supplier;
import java.util.regex.Pattern;

public final class Check {
    private static final Pattern PATTERN_ID = Pattern.compile("^[_a-zA-Z](?:[._a-zA-Z0-9]*[_a-zA-Z0-9]+)?$");
    private static final Pattern PATTERN_TEXT = Pattern.compile("^[^\n;]+$");

    private Check() {
        throw new IllegalStateException(getClass().getName());
    }

    public static boolean isFloat(@NotNull String val) {
        try {
            Float.parseFloat(val);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isIdentifier(@NotNull CharSequence id) {
        return PATTERN_ID.matcher(id).matches();
    }

    public static boolean isInteger(@NotNull String val) {
        try {
            Integer.parseInt(val);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isText(@NotNull CharSequence text) {
        return PATTERN_TEXT.matcher(text).matches();
    }

    @NotNull
    public static <T> T requireNonNull(@Nullable T obj, @Nullable Supplier<String> messageSupplier) {
        if (obj == null) {
            throw new AppException(messageSupplier == null ? "" : messageSupplier.get());
        }
        return obj;
    }

    @NotNull
    public static <T> T requireNonNull(@Nullable T obj,
                                       @PropertyKey(resourceBundle = "gargoyle.rpycg.ex.AppUserException")
                                               String messageKey, @NotNull String... args) {
        if (obj == null) {
            throw new AppUserException(messageKey, args);
        }
        return obj;
    }
}
