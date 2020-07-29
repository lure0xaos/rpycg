package gargoyle.rpycg.fx;

import java.text.MessageFormat;

class JULLogger implements Logger {
    private final java.util.logging.Logger logger;

    public JULLogger(String name) {
        logger = java.util.logging.Logger.getLogger(name);
    }

    @Override
    public void log(Level level, String format, Object... arguments) {
        logger.log(toLevel(level), MessageFormat.format(format, arguments));
    }

    private java.util.logging.Level toLevel(Level level) {
        switch (level) {
            case ERROR:
                return java.util.logging.Level.SEVERE;
            case WARN:
                return java.util.logging.Level.WARNING;
            case INFO:
                return java.util.logging.Level.INFO;
            case DEBUG:
                return java.util.logging.Level.FINE;
            case TRACE:
                return java.util.logging.Level.FINER;
        }
        throw new IllegalArgumentException(String.valueOf(level));
    }

    @Override
    public void log(Level level, String msg, Throwable t) {
        logger.log(toLevel(level), t, () -> msg);
    }
}
