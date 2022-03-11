package gargoyle.fx

import javafx.util.Callback

internal enum class FxCloseFlag {
    RESTART, PREVENT;

    @Suppress("UNCHECKED_CAST")
    fun doIf(context: FxContext): FxCloseAction {
        val properties = context.stage.properties
        if (properties.containsKey(this)) {
            val callback = properties[this] as Callback<FxContext, FxCloseAction>
            properties.remove(this)
            return callback.call(context)
        }
        return FxCloseAction.CLOSE
    }

    operator fun set(context: FxContext, callback: Callback<FxContext, FxCloseAction>) {
        context.stage.properties[this] = callback
    }
}
