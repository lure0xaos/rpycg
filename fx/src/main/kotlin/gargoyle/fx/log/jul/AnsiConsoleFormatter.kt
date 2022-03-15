package gargoyle.fx.log.jul

import java.text.DateFormat
import java.util.Date
import java.util.Locale

class AnsiConsoleFormatter : java.util.logging.Formatter() {
    override fun format(record: java.util.logging.LogRecord): String =
        "$ANSI_RESET$ANSI_WHITE${formatDate(record)} ${color(record)}${formatLevel(record)}$ANSI_WHITE [${
            formatSource(record)
        }] ${formatMessage(record)}${if (record.thrown != null) ":" else ""} ${
            formatThrowable(record)
        }$ANSI_RESET\n"

    private fun formatDate(record: java.util.logging.LogRecord): String =
        DateFormat.getDateTimeInstance(
            DateFormat.MEDIUM,
            DateFormat.MEDIUM,
            Locale.getDefault(Locale.Category.FORMAT)
        ).format(Date(record.millis))

    private fun formatLevel(record: java.util.logging.LogRecord): String =
        when (record.level) {
            java.util.logging.Level.INFO -> "INFO "
            java.util.logging.Level.WARNING -> "WARN "
            java.util.logging.Level.SEVERE -> "ERROR"
            java.util.logging.Level.FINE -> "DEBUG"
            java.util.logging.Level.FINER, java.util.logging.Level.FINEST -> "TRACE"
            else -> "     "
        }

    private fun formatSource(record: java.util.logging.LogRecord): String =
        with(record) {
            sourceClassName
                ?.let { sourceMethodName?.let { "$sourceClassName $sourceMethodName" } ?: sourceClassName }
                ?: loggerName
        }

    private fun formatThrowable(record: java.util.logging.LogRecord): String =
        with(record) { thrown?.let { thrown.stackTraceToString() } ?: "" }

    private fun color(record: java.util.logging.LogRecord): String =
        when (record.level) {
            java.util.logging.Level.INFO -> ANSI_CYAN
            java.util.logging.Level.WARNING -> ANSI_YELLOW
            java.util.logging.Level.SEVERE -> ANSI_RED
            java.util.logging.Level.FINE, java.util.logging.Level.FINER, java.util.logging.Level.FINEST -> ANSI_RESET
            else -> ANSI_RESET
        }

    companion object {
        const val ANSI_BLACK: String = "\u001B[30m"
        const val ANSI_BLACK_BACKGROUND: String = "\u001B[40m"
        const val ANSI_BLUE: String = "\u001B[34m"
        const val ANSI_BLUE_BACKGROUND: String = "\u001B[44m"
        const val ANSI_CYAN: String = "\u001B[36m"
        const val ANSI_CYAN_BACKGROUND: String = "\u001B[46m"
        const val ANSI_GREEN: String = "\u001B[32m"
        const val ANSI_GREEN_BACKGROUND: String = "\u001B[42m"
        const val ANSI_PURPLE: String = "\u001B[35m"
        const val ANSI_PURPLE_BACKGROUND: String = "\u001B[45m"
        const val ANSI_RED: String = "\u001B[31m"
        const val ANSI_RED_BACKGROUND: String = "\u001B[41m"
        const val ANSI_RESET: String = "\u001B[0m"
        const val ANSI_WHITE: String = "\u001B[37m"
        const val ANSI_WHITE_BACKGROUND: String = "\u001B[47m"
        const val ANSI_YELLOW: String = "\u001B[33m"
        const val ANSI_YELLOW_BACKGROUND: String = "\u001B[43m"
    }
}
