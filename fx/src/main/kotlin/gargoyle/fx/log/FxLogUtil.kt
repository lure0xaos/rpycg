package gargoyle.fx.log

import gargoyle.fx.FxUtil
import java.util.logging.LogRecord
import java.util.logging.Logger

internal object FxLogUtil {

    private val PACKAGES: Array<String> = arrayOf("java.", "javax.", FxLog::class.java.packageName)
    fun log(t: Throwable?, level: Level, format: String, arguments: Map<String, Any>) {
        (Thread.currentThread().stackTrace).firstOrNull { element: StackTraceElement ->
            PACKAGES.none { element.className.startsWith(it) }
        }?.let { doLog(it, t, level, format, arguments) }
    }

    private fun doLog(
        element: StackTraceElement,
        t: Throwable?,
        level: Level,
        format: String,
        arguments: Map<String, Any>
    ) {
        val logger = Logger.getLogger(element.className)
        val loggingLevel = translateLevel(level)
        if (logger.isLoggable(loggingLevel)) {
            val record = LogRecord(loggingLevel, FxUtil.format(format, arguments))
            record.sourceClassName = element.className
            record.sourceMethodName = element.methodName
            record.thrown = t
            logger.log(record)
        }
    }

    private fun translateLevel(level: Level?): java.util.logging.Level =
        when (level) {
            Level.ERROR -> java.util.logging.Level.SEVERE
            Level.WARN -> java.util.logging.Level.WARNING
            Level.INFO -> java.util.logging.Level.INFO
            Level.DEBUG -> java.util.logging.Level.FINE
            Level.TRACE -> java.util.logging.Level.FINER
            else -> java.util.logging.Level.FINEST
        }
}
