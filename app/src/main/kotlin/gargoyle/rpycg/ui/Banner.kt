package gargoyle.rpycg.ui

import gargoyle.fx.FxContext
import gargoyle.rpycg.ex.AppUserException
import javafx.scene.layout.BorderPane

class Banner : BorderPane() {
    init {
        FxContext.current.loadComponent(this) ?: throw AppUserException("No view {Banner}")
    }
}
