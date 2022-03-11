package gargoyle.fx.log

@Suppress("unused")
object FxLog {

    fun debug(msg: String) =
        log(Level.DEBUG, msg)

    fun debug(format: String, arguments: Map<String, Any>) =
        log(Level.DEBUG, format, arguments)

    fun debug(t: Throwable?, msg: String) =
        log(t, Level.DEBUG, msg)

    fun debug(t: Throwable?, msg: String, arguments: Map<String, Any>) =
        log(t, Level.DEBUG, msg, arguments)

    fun error(msg: String) =
        log(Level.ERROR, msg)

    fun error(format: String, arguments: Map<String, Any>) =
        log(Level.ERROR, format, arguments)

    fun error(t: Throwable?, msg: String) =
        log(t, Level.ERROR, msg)

    fun error(t: Throwable?, msg: String, arguments: Map<String, Any>) =
        log(t, Level.ERROR, msg, arguments)

    fun info(msg: String) =
        log(Level.INFO, msg)

    fun info(format: String, arguments: Map<String, Any>) =
        log(Level.INFO, format, arguments)

    fun info(t: Throwable?, msg: String) =
        log(t, Level.INFO, msg)

    fun info(t: Throwable?, msg: String, arguments: Map<String, Any>) =
        FxLogUtil.log(t, Level.INFO, msg, arguments)

    fun log(level: Level, msg: String) =
        FxLogUtil.log(null, level, msg, mapOf<String, Any>())

    fun log(t: Throwable?, level: Level, msg: String) =
        FxLogUtil.log(t, level, msg, java.util.Map.of<String, Any>())

    fun log(level: Level, format: String, arguments: Map<String, Any>) =
        FxLogUtil.log(null, level, format, arguments)

    fun log(t: Throwable?, level: Level, format: String, arguments: Map<String, Any>) =
        FxLogUtil.log(t, level, format, arguments)

    fun trace(msg: String) =
        log(Level.TRACE, msg)

    fun trace(format: String, arguments: Map<String, Any>) =
        log(Level.TRACE, format, arguments)

    fun trace(t: Throwable?, msg: String) =
        log(t, Level.TRACE, msg)

    fun trace(t: Throwable?, msg: String, arguments: Map<String, Any>) =
        log(t, Level.TRACE, msg, arguments)

    fun warn(msg: String) =
        log(Level.WARN, msg)

    fun warn(format: String, arguments: Map<String, Any>) =
        log(Level.WARN, format, arguments)

    fun warn(t: Throwable?, msg: String) =
        log(t, Level.WARN, msg)

    fun warn(t: Throwable?, msg: String, arguments: Map<String, Any>) =
        log(t, Level.WARN, msg, arguments)
}
