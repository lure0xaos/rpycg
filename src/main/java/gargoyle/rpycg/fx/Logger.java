package gargoyle.rpycg.fx;

public interface Logger {
    default void debug(String msg) {
        log(Level.DEBUG, msg);
    }

    default void debug(String format, Object arg) {
        log(Level.DEBUG, format, arg);
    }

    default void debug(String format, Object arg1, Object arg2) {
        log(Level.DEBUG, format, arg1, arg2);
    }

    default void debug(String format, Object... arguments) {
        log(Level.DEBUG, format, arguments);
    }

    default void debug(String msg, Throwable t) {
        log(Level.DEBUG, msg, t);
    }

    default void error(String msg) {
        log(Level.ERROR, msg);
    }

    default void error(String format, Object arg) {
        log(Level.ERROR, format, arg);
    }

    default void error(String format, Object arg1, Object arg2) {
        log(Level.ERROR, format, arg1, arg2);
    }

    default void error(String format, Object... arguments) {
        log(Level.ERROR, format, arguments);
    }

    default void error(String msg, Throwable t) {
        log(Level.ERROR, msg, t);
    }

    default void info(String msg) {
        log(Level.INFO, msg);
    }

    default void info(String format, Object arg) {
        log(Level.INFO, format, arg);
    }

    default void info(String format, Object arg1, Object arg2) {
        log(Level.INFO, format, arg1, arg2);
    }

    default void info(String format, Object... arguments) {
        log(Level.INFO, format, arguments);
    }

    default void info(String msg, Throwable t) {
        log(Level.INFO, msg, t);
    }

    default void trace(String msg) {
        log(Level.TRACE, msg);
    }

    default void log(Level level, String msg) {
        log(level, msg, new Object[0]);
    }

    void log(Level level, String format, Object... arguments);

    default void trace(String format, Object arg) {
        log(Level.TRACE, format, arg);
    }

    default void log(Level level, String format, Object arg) {
        log(level, format, new Object[]{arg});
    }

    default void trace(String format, Object arg1, Object arg2) {
        log(Level.TRACE, format, arg1, arg2);
    }

    default void log(Level level, String format, Object arg1, Object arg2) {
        log(level, format, new Object[]{arg1, arg2});
    }

    default void trace(String format, Object... arguments) {
        log(Level.TRACE, format, arguments);
    }

    default void trace(String msg, Throwable t) {
        log(Level.TRACE, msg, t);
    }

    void log(Level level, String msg, Throwable t);

    default void warn(String msg) {
        log(Level.WARN, msg);
    }

    default void warn(String format, Object arg) {
        log(Level.WARN, format, arg);
    }

    default void warn(String format, Object arg1, Object arg2) {
        log(Level.WARN, format, arg1, arg2);
    }

    default void warn(String format, Object... arguments) {
        log(Level.WARN, format, arguments);
    }

    default void warn(String msg, Throwable t) {
        log(Level.WARN, msg, t);
    }

    enum Level {
        ERROR, WARN, INFO, DEBUG, TRACE;
    }
}
