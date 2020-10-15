package gargoyle.rpycg.ui.model;

import javafx.scene.control.TreeCell;
import javafx.scene.input.DragEvent;
import org.jetbrains.annotations.NotNull;

public enum DROPPING {
    ONTO, ABOVE, BELOW;

    @SuppressWarnings("ConstantExpression")
    @NotNull
    public static DROPPING determineDropping(@NotNull TreeCell<DisplayItem> cell, @NotNull DragEvent event,
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
