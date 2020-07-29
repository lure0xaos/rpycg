package gargoyle.rpycg.ui.model;

import javafx.scene.control.TreeCell;
import javafx.scene.input.DragEvent;

public enum DROPPING {
    ONTO, ABOVE, BELOW;

    @SuppressWarnings("ConstantExpression")
    public static DROPPING determineDropping(TreeCell<DisplayItem> cell, DragEvent event,
                                             double bond) {
        double ratio = event.getY() / cell.getLayoutBounds().getHeight();
        if (ratio < bond) {
            return ABOVE;
        }
        if (ratio > 1 - bond) {
            return BELOW;
        }
        return ONTO;
    }
}
