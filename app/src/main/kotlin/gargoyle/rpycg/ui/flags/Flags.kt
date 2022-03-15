package gargoyle.rpycg.ui.flags

import gargoyle.fx.FxConstants
import gargoyle.fx.FxContext
import javafx.scene.image.ImageView

object Flags {

    fun getFlag(context: FxContext, flagBaseName: String): ImageView? =
        context.findResource(Flags::class, flagBaseName, FxConstants.EXT__IMAGES)?.toExternalForm()
            ?.let { ImageView(it) }
}
