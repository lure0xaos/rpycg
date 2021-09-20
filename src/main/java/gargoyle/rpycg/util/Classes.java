package gargoyle.rpycg.util;

import javafx.collections.ObservableList;
import javafx.css.Styleable;

public final class Classes {
    private Classes() {
        throw new IllegalStateException(Classes.class.getName());
    }

    public static void classAdd(final Styleable cell, final String className) {
        final ObservableList<String> styleClass = cell.getStyleClass();
        if (!styleClass.contains(className)) {
            styleClass.add(className);
        }
    }

    public static void classAddRemove(final Styleable cell,
                                      final String classNameAdd, final String classNameRemove) {
        classAdd(cell, classNameAdd);
        classRemove(cell, classNameRemove);
    }

    @SuppressWarnings("SameParameterValue")
    public static void classAddRemoveAll(final Styleable cell,
                                         final String classNameAdd, final String... classNameRemove) {
        classAdd(cell, classNameAdd);
        classRemoveAll(cell, classNameRemove);
    }

    @SuppressWarnings("MethodCallInLoopCondition")
    public static void classRemove(final Styleable cell, final String className) {
        final ObservableList<String> styleClass = cell.getStyleClass();
        while (styleClass.contains(className)) {
            styleClass.remove(className);
        }
    }

    public static void classRemoveAll(final Styleable cell, final String... classNames) {
        for (final String className : classNames) {
            classRemove(cell, className);
        }
    }
}
