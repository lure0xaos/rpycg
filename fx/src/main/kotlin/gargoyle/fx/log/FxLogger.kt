package gargoyle.fx.log

import java.net.URL

interface FxLogger {
    fun configure(configuration: URL)
    fun log(
        element: StackTraceElement?,
        level: FxLogLevel = FxLogLevel.INFO,
        message: () -> String = { "" },
        t: Throwable? = null
    )

}
