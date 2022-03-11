@file:Suppress("unused")

package gargoyle.fx.log

import java.util.Date
import java.util.logging.Formatter
import java.util.logging.LogRecord

class AnsiConsoleFormatter : Formatter() {
    override fun format(record: LogRecord): String =
        "$ANSI_RESET$ANSI_WHITE${formatDate(record)} ${
            colored(
                getAnsiColor(record),
                formatLevel(record)
            )
        } [${formatSource(record)}] ${formatMessage(record)}${if (record.thrown != null) ":" else ""} ${
            formatThrowable(
                record
            )
        }$ANSI_RESET\n"

    private fun colored(ansiColor: String, text: String): String =
        ansiColor + text + ANSI_WHITE

    private fun formatDate(record: LogRecord): String =
        Date(record.millis).toString()

    private fun formatLevel(record: LogRecord): String =
        when (record.level.toString()) {
            "INFO" -> "INFO "
            "WARNING" -> "WARN "
            "SEVERE" -> "ERROR"
            "FINE" -> "DEBUG"
            "FINER", "FINEST" -> "TRACE"
            else -> "     "
        }

    private fun formatLogger(record: LogRecord): String =
        record.loggerName

    private fun formatSource(record: LogRecord): String =
        if (null == record.sourceClassName) formatLogger(record)
        else
            if (null != record.sourceMethodName) "${record.sourceClassName} ${record.sourceMethodName}"
            else record.sourceClassName

    private fun formatThrowable(record: LogRecord): String =
        if (null == record.thrown) "" else record.thrown.stackTraceToString()

    private fun getAnsiColor(record: LogRecord): String =
        when (record.level.toString()) {
            "INFO" -> ANSI_CYAN
            "WARNING" -> ANSI_YELLOW
            "SEVERE" -> ANSI_RED
            "FINE", "FINER", "FINEST" -> ANSI_RESET
            else -> ANSI_RESET
        }

    companion object {
        const val ANSI_BLACK = "\u001B[30m"
        const val ANSI_BLACK_BACKGROUND = "\u001B[40m"
        const val ANSI_BLUE = "\u001B[34m"
        const val ANSI_BLUE_BACKGROUND = "\u001B[44m"
        const val ANSI_CYAN = "\u001B[36m"
        const val ANSI_CYAN_BACKGROUND = "\u001B[46m"
        const val ANSI_GREEN = "\u001B[32m"
        const val ANSI_GREEN_BACKGROUND = "\u001B[42m"
        const val ANSI_PURPLE = "\u001B[35m"
        const val ANSI_PURPLE_BACKGROUND = "\u001B[45m"
        const val ANSI_RED = "\u001B[31m"
        const val ANSI_RED_BACKGROUND = "\u001B[41m"
        const val ANSI_RESET = "\u001B[0m"
        const val ANSI_WHITE = "\u001B[37m"
        const val ANSI_WHITE_BACKGROUND = "\u001B[47m"
        const val ANSI_YELLOW = "\u001B[33m"
        const val ANSI_YELLOW_BACKGROUND = "\u001B[43m"
    }
}
