package gargoyle.rpycg.util

import javafx.css.Styleable

object Classes {

    fun classAdd(cell: Styleable, className: String) {
        if (!cell.styleClass.contains(className)) cell.styleClass.add(className)
    }

    fun classAddRemove(cell: Styleable, classNameAdd: String, classNameRemove: String) {
        if (!cell.styleClass.contains(classNameAdd)) cell.styleClass.add(classNameAdd)
        while (cell.styleClass.contains(classNameRemove)) cell.styleClass.remove(classNameRemove)
    }

    fun classAddRemoveAll(cell: Styleable, classNameAdd: String, vararg classNameRemove: String) {
        if (!cell.styleClass.contains(classNameAdd)) cell.styleClass.add(classNameAdd)
        for (className in classNameRemove) while (cell.styleClass.contains(className)) cell.styleClass.remove(className)
    }

    fun classRemove(cell: Styleable, className: String) {
        while (cell.styleClass.contains(className)) cell.styleClass.remove(className)
    }

    fun classRemoveAll(cell: Styleable, vararg classNames: String) {
        for (className in classNames) while (cell.styleClass.contains(className)) cell.styleClass.remove(className)
    }

}
