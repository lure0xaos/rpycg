package gargoyle.rpycg.ui

import gargoyle.fx.FxContext
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Hyperlink
import javafx.scene.layout.GridPane

class About : GridPane() {
    @FXML
    private lateinit var link: Hyperlink

    init {
        FxContext.current.loadComponent(this) ?: error("No view {About}")
    }

    @Suppress("UNUSED_PARAMETER")
    @FXML
    fun onLink(e: ActionEvent): Unit =
        FxContext.current.showDocument(link.userData.toString())
}
