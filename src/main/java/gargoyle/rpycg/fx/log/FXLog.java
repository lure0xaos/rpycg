package gargoyle.rpycg.fx.log;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

@SuppressWarnings({"unused", "ClassWithTooManyMethods"})
public final class FXLog {
    private FXLog() {
        throw new IllegalStateException(FXLog.class.getName());
    }

    private static final String[] PACKAGES = {"java.", "javax.", FXLog.class.getPackageName()};

    public static void debug(final String msg) {
        log(Level.DEBUG, msg);
    }

    public static void debug(final String format, final Object arg) {
        log(Level.DEBUG, format, arg);
    }

    public static void debug(final String format, final Object arg1, final Object arg2) {
        log(Level.DEBUG, format, arg1, arg2);
    }

    public static void debug(final String format, final Object... arguments) {
        log(Level.DEBUG, format, arguments);
    }

    public static void debug(final Throwable t, final String msg) {
        log(t, Level.DEBUG, msg);
    }

    public static void debug(final Throwable t, final String msg, final Object... arguments) {
        log(t, Level.DEBUG, msg, arguments);
    }

    public static void error(final String msg) {
        log(Level.ERROR, msg);
    }

    public static void error(final String format, final Object arg) {
        log(Level.ERROR, format, arg);
    }

    public static void error(final String format, final Object arg1, final Object arg2) {
        log(Level.ERROR, format, arg1, arg2);
    }

    public static void error(final String format, final Object... arguments) {
        log(Level.ERROR, format, arguments);
    }

    public static void error(final Throwable t, final String msg) {
        log(t, Level.ERROR, msg);
    }

    public static void error(final Throwable t, final String msg, final Object... arguments) {
        log(t, Level.ERROR, msg, arguments);
    }

    public static void info(final String msg) {
        log(Level.INFO, msg);
    }

    public static void info(final String format, final Object arg) {
        log(Level.INFO, format, arg);
    }

    public static void info(final String format, final Object arg1, final Object arg2) {
        log(Level.INFO, format, arg1, arg2);
    }

    public static void info(final String format, final Object... arguments) {
        log(Level.INFO, format, arguments);
    }

    public static void info(final Throwable t, final String msg) {
        log(t, Level.INFO, msg);
    }

    public static void info(final Throwable t, final String msg, final Object... arguments) {
        log(t, Level.INFO, msg, arguments);
    }

    public static void log(final Level level, final String msg) {
        log(level, msg, new Object[0]);
    }

    public static void log(final Level level, final String format, final Object arg) {
        log(level, format, new Object[]{arg});
    }

    public static void log(final Level level, final String format, final Object arg1, final Object arg2) {
        log(level, format, new Object[]{arg1, arg2});
    }

    public static void log(final Throwable t, final Level level, final String msg) {
        log(t, level, msg, new Object[0]);
    }

    public static void log(final Level level, final String format, final Object... arguments) {
        log(null, level, format, arguments);
    }

    public static void log(final Throwable t, final Level level, final String format, final Object... arguments) {
        Arrays.stream(Thread.currentThread().getStackTrace()).filter(element ->
                        Arrays.stream(PACKAGES).noneMatch(packageName -> element.getClassName().startsWith(packageName)))
                .findFirst().ifPresent(element -> doLog(element, t, level, format, arguments));
    }

    private static void doLog(final StackTraceElement element,
                              final Throwable t, final Level level, final String format, final Object[] arguments) {
        final Logger logger = Logger.getLogger(element.getClassName());
        final java.util.logging.Level loggingLevel = translateLevel(level);
        if (logger.isLoggable(loggingLevel)) {
            final LogRecord record = new LogRecord(loggingLevel, MessageFormat.format(format, arguments));
            record.setSourceClassName(element.getClassName());
            record.setSourceMethodName(element.getMethodName());
            record.setThrown(t);
            logger.log(record);
        }
    }

    private static java.util.logging.Level translateLevel(final Level level) {
        final java.util.logging.Level loggingLevel;
        switch (level) {
            case ERROR:
                loggingLevel = java.util.logging.Level.SEVERE;
                break;
            case WARN:
                loggingLevel = java.util.logging.Level.WARNING;
                break;
            case INFO:
                loggingLevel = java.util.logging.Level.INFO;
                break;
            case DEBUG:
                loggingLevel = java.util.logging.Level.FINE;
                break;
            case TRACE:
                loggingLevel = java.util.logging.Level.FINER;
                break;
            default:
                loggingLevel = java.util.logging.Level.FINEST;
                break;
        }
        return loggingLevel;
    }

    public static void trace(final String msg) {
        log(Level.TRACE, msg);
    }

    public static void trace(final String format, final Object arg) {
        log(Level.TRACE, format, arg);
    }

    public static void trace(final String format, final Object arg1, final Object arg2) {
        log(Level.TRACE, format, arg1, arg2);
    }

    public static void trace(final String format, final Object... arguments) {
        log(Level.TRACE, format, arguments);
    }

    public static void trace(final Throwable t, final String msg) {
        log(t, Level.TRACE, msg);
    }

    public static void trace(final Throwable t, final String msg, final Object... arguments) {
        log(t, Level.TRACE, msg, arguments);
    }

    public static void warn(final String msg) {
        log(Level.WARN, msg);
    }

    public static void warn(final String format, final Object arg) {
        log(Level.WARN, format, arg);
    }

    public static void warn(final String format, final Object arg1, final Object arg2) {
        log(Level.WARN, format, arg1, arg2);
    }

    public static void warn(final String format, final Object... arguments) {
        log(Level.WARN, format, arguments);
    }

    public static void warn(final Throwable t, final String msg) {
        log(t, Level.WARN, msg);
    }

    public static void warn(final Throwable t, final String msg, final Object... arguments) {
        log(t, Level.WARN, msg, arguments);
    }

}
