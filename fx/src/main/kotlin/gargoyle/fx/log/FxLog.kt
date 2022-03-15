package gargoyle.fx.log

object FxLog {

    fun error(message: String): Unit =
        log(FxLogLevel.ERROR, message, null)

    fun error(message: String, t: Throwable? = null): Unit =
        log(FxLogLevel.ERROR, message, t)

    fun error(message: () -> String): Unit =
        log(FxLogLevel.ERROR, message, null)

    fun error(message: () -> String, t: Throwable? = null): Unit =
        log(FxLogLevel.ERROR, message, t)

    fun warn(message: String): Unit =
        log(FxLogLevel.WARN, message, null)

    fun warn(message: String, t: Throwable? = null): Unit =
        log(FxLogLevel.WARN, message, t)

    fun warn(message: () -> String): Unit =
        log(FxLogLevel.WARN, message, null)

    fun warn(message: () -> String, t: Throwable? = null): Unit =
        log(FxLogLevel.WARN, message, t)

    fun info(message: String): Unit =
        log(FxLogLevel.INFO, message, null)

    fun info(message: String, t: Throwable? = null): Unit =
        log(FxLogLevel.INFO, message, t)

    fun info(message: () -> String): Unit =
        log(FxLogLevel.INFO, message, null)

    fun info(message: () -> String, t: Throwable? = null): Unit =
        log(FxLogLevel.INFO, message, t)

    fun debug(message: String): Unit =
        log(FxLogLevel.DEBUG, message, null)

    fun debug(message: String, t: Throwable? = null): Unit =
        log(FxLogLevel.DEBUG, message, t)

    fun debug(message: () -> String): Unit =
        log(FxLogLevel.DEBUG, message, null)

    fun debug(message: () -> String, t: Throwable? = null): Unit =
        log(FxLogLevel.DEBUG, message, t)

    fun trace(message: String): Unit =
        log(FxLogLevel.TRACE, message, null)

    fun trace(message: String, t: Throwable? = null): Unit =
        log(FxLogLevel.TRACE, message, t)

    fun trace(message: () -> String): Unit =
        log(FxLogLevel.TRACE, message, null)

    fun trace(message: () -> String, t: Throwable? = null): Unit =
        log(FxLogLevel.TRACE, message, t)

    fun log(level: FxLogLevel, message: String): Unit =
        FxLoggerFactory.logger.log(stackTraceElement, level, { message }, null)

    fun log(level: FxLogLevel, message: String, t: Throwable? = null): Unit =
        FxLoggerFactory.logger.log(stackTraceElement, level, { message }, t)

    fun log(level: FxLogLevel, message: () -> String): Unit =
        FxLoggerFactory.logger.log(stackTraceElement, level, message, null)

    fun log(level: FxLogLevel, message: () -> String, t: Throwable? = null): Unit =
        FxLoggerFactory.logger.log(stackTraceElement, level, message, t)

    private val stackTraceElement: StackTraceElement?
        get() = Thread.currentThread().stackTrace.firstOrNull { element: StackTraceElement ->
            arrayOf("java.", "javax.", FxLog::class.java.packageName).none { element.className.startsWith(it) }
        }
}
