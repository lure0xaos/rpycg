package gargoyle.fx.icons

import gargoyle.fx.FxConstants
import gargoyle.fx.FxContext
import gargoyle.fx.FxUtil
import javafx.scene.control.ButtonBar.ButtonData
import java.net.URL

enum class Icon(private val value: String, vararg buttonData: ButtonData) {
    CANCEL("cancel", ButtonData.CANCEL_CLOSE, ButtonData.NO), EMPTY("empty"), OK(
        "ok",
        ButtonData.OK_DONE,
        ButtonData.YES
    );

    private val buttonData: Array<ButtonData> = arrayOf(*buttonData)

    fun findIcon(context: FxContext): URL? {
        val iconBaseName: String = FxUtil.resolveBaseName(Icon::class, value)
        return FxUtil.findResource(Icon::class, context.locale, iconBaseName, FxConstants.EXT__IMAGES)
            ?: FxUtil.findResource(context.baseClass, context.locale, iconBaseName, FxConstants.EXT__IMAGES)
    }

    companion object {
        fun find(buttonData: ButtonData): Icon =
            values().firstOrNull { it.buttonData.any { data: ButtonData -> buttonData == data } } ?: EMPTY
    }
}
