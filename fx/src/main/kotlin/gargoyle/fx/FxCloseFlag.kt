package gargoyle.fx

internal enum class FxCloseFlag {
    RESTART, PREVENT;

    @Suppress("UNCHECKED_CAST")
    fun doIf(context: FxContext): FxCloseAction =
        context.stage.properties.let { properties ->
            if (properties.containsKey(this)) {
                (properties[this] as (FxContext) -> FxCloseAction)(context).also {
                    properties.remove(this)
                }
            } else
                FxCloseAction.CLOSE
        }

    operator fun set(context: FxContext, callback: (FxContext) -> FxCloseAction) {
        context.stage.properties[this] = callback
    }
}
