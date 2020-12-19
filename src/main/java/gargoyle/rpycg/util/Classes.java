package gargoyle.rpycg.util;

import javafx.collections.ObservableList;
import javafx.css.Styleable;

public final class Classes {
    private Classes() {
        throw new IllegalStateException(Classes.class.getName());
    }

    public static void classAddRemove(Styleable cell,
                                      String classNameAdd, String classNameRemove) {
        classAdd(cell, classNameAdd);
        classRemove(cell, classNameRemove);
    }

    public static void classAdd(Styleable cell, String className) {
        ObservableList<String> styleClass = cell.getStyleClass();
        if (!styleClass.contains(className)) {
            styleClass.add(className);
        }
    }

    @SuppressWarnings("MethodCallInLoopCondition")
    public static void classRemove(Styleable cell, String className) {
        ObservableList<String> styleClass = cell.getStyleClass();
        while (styleClass.contains(className)) {
            styleClass.remove(className);
        }
    }

    @SuppressWarnings("SameParameterValue")
    public static void classAddRemoveAll(Styleable cell,
                                         String classNameAdd, String... classNameRemove) {
        classAdd(cell, classNameAdd);
        classRemoveAll(cell, classNameRemove);
    }

    public static void classRemoveAll(Styleable cell, String... classNames) {
        for (String className : classNames) {
            classRemove(cell, className);
        }
    }
}
