package gargoyle.fx.log

import java.io.IOException
import java.net.URL

object FxLoggerFactory {
    internal lateinit var logger: FxLogger
        private set

    fun configure(logger: FxLogger, configuration: URL?) {
        this.logger = logger
        configuration?.also {
            try {
                this.logger.configure(it)
            } catch (e: IOException) {
                FxLog.error("$configuration initialization error", e)
            }
        } ?: FxLog.error("$logger initialization error")
    }
}
