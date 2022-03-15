package gargoyle.rpycg.ui

import gargoyle.fx.FxContext
import javafx.scene.layout.BorderPane

class Banner : BorderPane() {
    init {
        FxContext.current.loadComponent(this) ?: error("No view {Banner}")
    }
}
