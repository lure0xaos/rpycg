package gargoyle.rpycg.fx.log;

import gargoyle.rpycg.fx.FXUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

@SuppressWarnings("unused")
public final class AnsiConsoleFormatter extends Formatter {

    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";

    @Override
    public String format(final LogRecord record) {
        return ANSI_RESET + ANSI_WHITE + formatDate(record) + " " + colored(getAnsiColor(record), formatLevel(record))
                + " [" + formatSource(record) + "] "
                + formatMessage(record) + ": " + formatThrowable(record) + ANSI_RESET + "\n";
    }

    private String colored(final String ansiColor, final String text) {
        return ansiColor + text + ANSI_WHITE;
    }

    private String formatDate(final LogRecord record) {
        return new Date(record.getMillis()).toString();
    }

    private String formatLevel(final LogRecord record) {
        final String ansiColor = record.getLevel().getLocalizedName();
        switch (record.getLevel().toString()) {
            case "INFO":
                return "INFO ";
            case "WARNING":
                return "WARN ";
            case "SEVERE":
                return "ERROR";
            case "FINE":
                return "DEBUG";
            case "FINER":
            case "FINEST":
                return "TRACE";
            default:
                return "     ";
        }
    }

    private String formatLogger(final LogRecord record) {
        return record.getLoggerName();
    }

    private String formatSource(final LogRecord record) {
        if (null == record.getSourceClassName()) {
            return formatLogger(record);
        } else {
            if (null != record.getSourceMethodName()) {
                return record.getSourceClassName() + " " + record.getSourceMethodName();
            } else {
                return record.getSourceClassName();
            }
        }
    }

    private String formatThrowable(final LogRecord record) {
        return null == record.getThrown() ? "" : FXUtil.stringStackTrace(record.getThrown());
    }

    private String getAnsiColor(final LogRecord record) {
        switch (record.getLevel().toString()) {
            case "INFO":
                return ANSI_CYAN;
            case "WARNING":
                return ANSI_YELLOW;
            case "SEVERE":
                return ANSI_RED;
            case "FINE":
            case "FINER":
            case "FINEST":
            default:
                return ANSI_RESET;
        }
    }

}
