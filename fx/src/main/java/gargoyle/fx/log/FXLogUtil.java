package gargoyle.fx.log;

import gargoyle.fx.FXUtil;

import java.util.Arrays;
import java.util.Map;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

final class FXLogUtil {
    private static final String[] PACKAGES = {"java.", "javax.", FXLog.class.getPackageName()};

    private FXLogUtil() {
        throw new IllegalStateException(FXLogUtil.class.getName());
    }

    static void log(final Throwable t, final Level level, final String format, final Map<String, ?> arguments) {
        Arrays.stream(Thread.currentThread().getStackTrace()).filter(element ->
                        Arrays.stream(PACKAGES).noneMatch(packageName -> element.getClassName().startsWith(packageName)))
                .findFirst().ifPresent(element -> doLog(element, t, level, format, arguments));
    }

    private static void doLog(final StackTraceElement element,
                              final Throwable t, final Level level, final String format, final Map<String, ?> arguments) {
        final Logger logger = Logger.getLogger(element.getClassName());
        final java.util.logging.Level loggingLevel = translateLevel(level);
        if (logger.isLoggable(loggingLevel)) {
            final LogRecord record = new LogRecord(loggingLevel, FXUtil.format(format, arguments));
            record.setSourceClassName(element.getClassName());
            record.setSourceMethodName(element.getMethodName());
            record.setThrown(t);
            logger.log(record);
        }
    }

    private static java.util.logging.Level translateLevel(final Level level) {
        return switch (level) {
            case ERROR -> java.util.logging.Level.SEVERE;
            case WARN -> java.util.logging.Level.WARNING;
            case INFO -> java.util.logging.Level.INFO;
            case DEBUG -> java.util.logging.Level.FINE;
            case TRACE -> java.util.logging.Level.FINER;
            default -> java.util.logging.Level.FINEST;
        };
    }
}
