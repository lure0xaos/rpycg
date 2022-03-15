package gargoyle.rpycg.ui.model

import gargoyle.rpycg.model.ModelType
import gargoyle.rpycg.model.VarType
import javafx.beans.property.Property
import javafx.beans.property.ReadOnlyProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.TreeItem

@Suppress("MemberVisibilityCanBePrivate")
class DisplayItem private constructor(modelType: ModelType, label: String, name: String, value: String, type: VarType) {

    val label: Property<String>
    val modelType: ReadOnlyProperty<ModelType>
    val name: Property<String>
    val type: Property<VarType>
    val value: Property<String>

    fun copyOf(): DisplayItem =
        DisplayItem(modelType.value, label.value, name.value, value.value, type.value)

    fun getLabel(): String =
        label.value

    fun setLabel(label: String) {
        this.label.value = label
    }

    fun getModelType(): ModelType =
        modelType.value

    fun getName(): String =
        name.value

    fun setName(name: String) {
        this.name.value = name
    }

    fun getType(): VarType =
        type.value

    fun setType(type: VarType) {
        this.type.value = type
    }

    fun getValue(): String =
        value.value

    fun setValue(value: String) {
        this.value.value = value
    }

    override fun hashCode(): Int =
        arrayOf(modelType, name).contentHashCode()

    override fun equals(other: Any?): Boolean =
        if (this === other) true else if (null == other || DisplayItem::class.java != other.javaClass) false
        else modelType.value === (other as DisplayItem).modelType.value && name.value == (other as DisplayItem).name.value

    override fun toString(): String =
        "DisplayItem[${modelType.value}]{type=${type.value}, label=${label.value}, name=${name.value}, value=${value.value}}"

    fun labelProperty(): Property<String> =
        label

    fun modelTypeProperty(): ReadOnlyProperty<ModelType> =
        modelType

    fun nameProperty(): Property<String> =
        name

    fun typeProperty(): Property<VarType> =
        type

    fun valueProperty(): Property<String> =
        value

    private class DisplayTreeItem(item: DisplayItem) : TreeItem<DisplayItem>(item) {
        override fun toString(): String =
            "TreeItem: $value"
    }

    companion object {
        fun createMenu(label: String, name: String): DisplayItem =
            DisplayItem(ModelType.MENU, label, name, "", VarType.STR)

        fun createRoot(): DisplayItem = createMenu("", "")

        fun createVariable(type: VarType, label: String, name: String, value: String): DisplayItem =
            DisplayItem(ModelType.VARIABLE, label, name, value, type)

        fun toTreeItem(item: DisplayItem, expanded: Boolean): TreeItem<DisplayItem> {
            val treeItem: TreeItem<DisplayItem> = DisplayTreeItem(item)
            treeItem.isExpanded = expanded
            return treeItem
        }
    }

    init {
        this.modelType = SimpleObjectProperty(modelType)
        this.label = SimpleStringProperty(label)
        this.name = SimpleStringProperty(name)
        this.value = SimpleStringProperty(value)
        this.type = SimpleObjectProperty(type)
    }
}
