package gargoyle.rpycg.ui.model

import javafx.scene.control.TreeCell
import javafx.scene.input.DragEvent

enum class DROPPING {
    ONTO, ABOVE, BELOW;

    companion object {
        fun determineDropping(cell: TreeCell<DisplayItem>, event: DragEvent, bond: Double): DROPPING {
            val ratio = event.y / cell.layoutBounds.height
            return if (ratio < bond) {
                ABOVE
            } else if (ratio > 1 - bond) {
                BELOW
            } else ONTO
        }
    }
}
