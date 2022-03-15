package gargoyle.rpycg.service

import gargoyle.rpycg.model.ModelItem
import gargoyle.rpycg.model.ModelType
import gargoyle.rpycg.ui.model.DisplayItem
import javafx.scene.control.TreeItem

class ModelConverter {
    @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
    fun toModel(root: TreeItem<DisplayItem>): ModelItem {
        val parent = ModelItem.createMenu(root.value.label.value, root.value.name.value)
        root.children.forEach {
            when (it.value.modelType.value) {
                ModelType.MENU -> parent.addChild(toModel(it))
                ModelType.VARIABLE -> parent.addChild(
                    ModelItem.createVariable(
                        it.value.type.value,
                        it.value.label.value,
                        it.value.name.value,
                        it.value.value.value
                    )
                )
            }
        }
        return parent
    }

    fun toTree(item: ModelItem): TreeItem<DisplayItem> =
        when (item.modelType) {
            ModelType.MENU -> {
                val children: Set<ModelItem> = item.getChildren()
                val treeItem: TreeItem<DisplayItem> =
                    DisplayItem.toTreeItem(
                        DisplayItem.createMenu(item.label, item.name),
                        null == item.parent || children.isEmpty()
                    )
                children.forEach { treeItem.children += toTree(it) }
                treeItem
            }
            ModelType.VARIABLE -> {
                DisplayItem.toTreeItem(DisplayItem.createVariable(item.type, item.label, item.name, item.value), false)
            }
        }
}
