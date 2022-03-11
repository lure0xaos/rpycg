package gargoyle.rpycg.ui.icons

import gargoyle.fx.FxConstants
import gargoyle.fx.FxContext
import java.net.URL

@Suppress("unused")
enum class Icon(private val value: String) {
    COMPUTER("computer"),
    DELETE("clear"),
    EMPTY("empty"), FILE("text-x-generic"),
    FOLDER("folder"),
    FOLDER_OPEN("folder-open"),
    GAME_FOLDER("game-folder"),
    GAME_FOLDER_OPEN("game-folder-open"),
    MENU("menu"),
    TEMPLATE("template"),
    VARIABLE("var");

    fun findIcon(context: FxContext): URL? = context.findResource(Icon::class, value, FxConstants.EXT__IMAGES)
}
