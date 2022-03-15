package gargoyle.fx.log;

import java.util.Map;

@SuppressWarnings({"unused", "ClassWithTooManyMethods"})
public final class FXLog {

    private FXLog() {
        throw new IllegalStateException(FXLog.class.getName());
    }

    public static void debug(final String msg) {
        log(Level.DEBUG, msg);
    }

    public static void debug(final String format, final Map<String, ?> arguments) {
        log(Level.DEBUG, format, arguments);
    }

    public static void debug(final Throwable t, final String msg) {
        log(t, Level.DEBUG, msg);
    }

    public static void debug(final Throwable t, final String msg, final Map<String, ?> arguments) {
        log(t, Level.DEBUG, msg, arguments);
    }

    public static void error(final String msg) {
        log(Level.ERROR, msg);
    }

    public static void error(final String format, final Map<String, ?> arguments) {
        log(Level.ERROR, format, arguments);
    }

    public static void error(final Throwable t, final String msg) {
        log(t, Level.ERROR, msg);
    }

    public static void error(final Throwable t, final String msg, final Map<String, ?> arguments) {
        log(t, Level.ERROR, msg, arguments);
    }

    public static void info(final String msg) {
        log(Level.INFO, msg);
    }

    public static void info(final String format, final Map<String, ?> arguments) {
        log(Level.INFO, format, arguments);
    }

    public static void info(final Throwable t, final String msg) {
        log(t, Level.INFO, msg);
    }

    public static void info(final Throwable t, final String msg, final Map<String, ?> arguments) {
        FXLogUtil.log(t, Level.INFO, msg, arguments);
    }

    public static void log(final Level level, final String msg) {
        FXLogUtil.log(null, level, msg, Map.of());
    }

    public static void log(final Throwable t, final Level level, final String msg) {
        FXLogUtil.log(t, level, msg, Map.of());
    }

    public static void log(final Level level, final String format, final Map<String, ?> arguments) {
        FXLogUtil.log(null, level, format, arguments);
    }

    public static void log(final Throwable t, final Level level, final String format, final Map<String, ?> arguments) {
        FXLogUtil.log(t, level, format, arguments);
    }

    public static void trace(final String msg) {
        log(Level.TRACE, msg);
    }

    public static void trace(final String format, final Map<String, ?> arguments) {
        log(Level.TRACE, format, arguments);
    }

    public static void trace(final Throwable t, final String msg) {
        log(t, Level.TRACE, msg);
    }

    public static void trace(final Throwable t, final String msg, final Map<String, ?> arguments) {
        log(t, Level.TRACE, msg, arguments);
    }

    public static void warn(final String msg) {
        log(Level.WARN, msg);
    }

    public static void warn(final String format, final Map<String, ?> arguments) {
        log(Level.WARN, format, arguments);
    }

    public static void warn(final Throwable t, final String msg) {
        log(t, Level.WARN, msg);
    }

    public static void warn(final Throwable t, final String msg, final Map<String, ?> arguments) {
        log(t, Level.WARN, msg, arguments);
    }

}
