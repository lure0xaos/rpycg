package gargoyle.fx.icons

import gargoyle.fx.FxConstants
import gargoyle.fx.FxContext
import gargoyle.fx.FxUtil
import javafx.scene.control.ButtonBar.ButtonData
import java.net.URL

enum class Icon(private val value: String, vararg buttonData: ButtonData) {
    CANCEL("cancel", ButtonData.CANCEL_CLOSE, ButtonData.NO),
    EMPTY("empty"),
    OK("ok", ButtonData.OK_DONE, ButtonData.YES);

    private val buttonData: Array<ButtonData> = arrayOf(*buttonData)

    fun findIcon(context: FxContext): URL? =
        FxUtil.resolveBaseName(Icon::class, value).let {
            FxUtil.findResource(Icon::class, context.locale, it, FxConstants.EXT__IMAGES)
                ?: FxUtil.findResource(context.baseClass, context.locale, it, FxConstants.EXT__IMAGES)
        }

    companion object {
        fun find(buttonData: ButtonData): Icon =
            values().firstOrNull { icon -> icon.buttonData.any { buttonData == it } } ?: EMPTY
    }
}
