package gargoyle.rpycg.util;

import javafx.collections.ObservableList;
import javafx.css.Styleable;
import org.jetbrains.annotations.NotNull;

public final class Classes {
    private Classes() {
        throw new IllegalStateException(Classes.class.getName());
    }

    public static void classAddRemove(@NotNull Styleable cell,
                                      @NotNull String classNameAdd, @NotNull String classNameRemove) {
        classAdd(cell, classNameAdd);
        classRemove(cell, classNameRemove);
    }

    public static void classAdd(@NotNull Styleable cell, @NotNull String className) {
        ObservableList<String> styleClass = cell.getStyleClass();
        if (!styleClass.contains(className)) {
            styleClass.add(className);
        }
    }

    @SuppressWarnings("MethodCallInLoopCondition")
    public static void classRemove(@NotNull Styleable cell, @NotNull String className) {
        ObservableList<String> styleClass = cell.getStyleClass();
        while (styleClass.contains(className)) {
            styleClass.remove(className);
        }
    }

    @SuppressWarnings("SameParameterValue")
    public static void classAddRemoveAll(@NotNull Styleable cell,
                                         @NotNull String classNameAdd, @NotNull String... classNameRemove) {
        classAdd(cell, classNameAdd);
        classRemoveAll(cell, classNameRemove);
    }

    public static void classRemoveAll(@NotNull Styleable cell, @NotNull String... classNames) {
        for (String className : classNames) {
            classRemove(cell, className);
        }
    }
}
