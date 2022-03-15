package gargoyle.fx.log.jul

import gargoyle.fx.log.FxLogLevel
import gargoyle.fx.log.FxLogger
import java.net.URL
import java.util.MissingResourceException
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.LogRecord
import java.util.logging.Logger

internal object FxJULLogger : FxLogger {

    override fun log(element: StackTraceElement?, level: FxLogLevel, message: () -> String, t: Throwable?) {
        element?.let {
            val logger = try {
                Logger.getLogger(it.className, it.className)
            } catch (e: MissingResourceException) {
                Logger.getLogger(it.className)
            }
            val loggingLevel = when (level) {
                FxLogLevel.ERROR -> Level.SEVERE
                FxLogLevel.WARN -> Level.WARNING
                FxLogLevel.INFO -> Level.INFO
                FxLogLevel.DEBUG -> Level.FINE
                FxLogLevel.TRACE -> Level.FINER
            }
            if (logger.isLoggable(loggingLevel)) {
                with(LogRecord(loggingLevel, message())) {
                    sourceClassName = it.className
                    sourceMethodName = it.methodName
                    thrown = t
                    logger.log(this)
                }
            }
            Unit
        }
    }


    override fun configure(configuration: URL) {
        configuration.openStream()?.use { LogManager.getLogManager().readConfiguration(it) }
    }
}
