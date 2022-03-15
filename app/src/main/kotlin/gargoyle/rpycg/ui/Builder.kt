package gargoyle.rpycg.ui

import gargoyle.fx.FxComponent
import gargoyle.fx.FxContext
import gargoyle.fx.FxRun
import gargoyle.fx.FxUtil.findStage
import gargoyle.fx.FxUtil.get
import gargoyle.rpycg.model.ModelItem
import gargoyle.rpycg.model.ModelTemplate
import gargoyle.rpycg.model.ModelType
import gargoyle.rpycg.service.ErrorMailer.mailError
import gargoyle.rpycg.service.ModelConverter
import gargoyle.rpycg.ui.icons.Icon
import gargoyle.rpycg.ui.model.DROPPING
import gargoyle.rpycg.ui.model.DisplayItem
import gargoyle.rpycg.ui.model.FULLNESS
import gargoyle.rpycg.util.Classes.classAdd
import gargoyle.rpycg.util.Classes.classAddRemove
import gargoyle.rpycg.util.Classes.classAddRemoveAll
import gargoyle.rpycg.util.Classes.classRemove
import gargoyle.rpycg.util.Classes.classRemoveAll
import gargoyle.rpycg.util.TreeItemWalker.Companion.visitItems
import javafx.animation.Animation
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.css.Styleable
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.ScrollBar
import javafx.scene.control.ScrollPane
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.control.Tooltip
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.control.skin.TreeViewSkin
import javafx.scene.image.ImageView
import javafx.scene.input.ClipboardContent
import javafx.scene.input.DataFormat
import javafx.scene.input.DragEvent
import javafx.scene.input.MouseEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import javafx.util.Duration
import java.net.URL
import java.util.ResourceBundle
import kotlin.math.ceil

class Builder : ScrollPane(), Initializable {
    private val changed = SimpleBooleanProperty(false)

    @Suppress("UNNECESSARY_LATEINIT", "JoinDeclarationAndAssignment")
    private lateinit var component: FxComponent<Builder, ScrollPane>
    private val modelConverter: ModelConverter = ModelConverter()
    private val scrollTimeline = Timeline()
    private lateinit var resources: ResourceBundle
    private var scrollDirection = 0.0

    @FXML
    private lateinit var tree: TreeView<DisplayItem>

    init {
        component = FxContext.current.loadComponent(this) ?: error("No view {Builder}")
    }

    fun addRootMenu() {
        addMenu(root!!)
    }

    fun addRootVariable() {
        addVariable(root!!)
    }

    fun changedProperty(): SimpleBooleanProperty =
        changed

    fun clearAll() {
        changed.value = shouldClearRoot()
    }

    var model: ModelItem
        get() = modelConverter.toModel(tree.root)
        set(value) {
            FxRun.runLater { changed.value = shouldUpdateTree(modelConverter.toTree(value)) }
        }

    override fun initialize(location: URL, resources: ResourceBundle) {
        this.resources = resources
        initializeTree(createPlaceHolder(resources))
    }

    fun isChanged(): Boolean =
        changed.value

    fun setChanged(changed: Boolean) {
        this.changed.value = changed
    }

    val isTreeEmpty: Boolean
        get() = tree.root.children.isEmpty()

    private fun addItem(item: TreeItem<DisplayItem>, displayItem: DisplayItem, expanded: Boolean) {
        val newItem: TreeItem<DisplayItem> = DisplayItem.toTreeItem(displayItem, expanded)
        item.children.add(newItem)
        changed.value = true
        selectItem(newItem)
    }

    private fun addMenu(item: TreeItem<DisplayItem>) {
        val dialog = MenuDialog()
        dialog.setKnown(getKnownNames(""))
        dialog.initOwner(stage)
        dialog.showAndWait().ifPresent { addItem(item, it, true) }
    }

    private fun addVariable(item: TreeItem<DisplayItem>) {
        val dialog = VariableDialog()
        dialog.initOwner(stage)
        dialog.showAndWait().ifPresent { addItem(item, it, false) }
    }

    private fun attachTo(destItem: TreeItem<DisplayItem>, dragItem: TreeItem<DisplayItem>) {
        dragItem.parent.children.remove(dragItem)
        destItem.children.add(dragItem)
        selectItem(dragItem)
    }

    private fun createContextMenuForCell(treeItem: TreeItem<DisplayItem>): ContextMenu {
        val menuItems: MutableList<MenuItem> = FXCollections.observableArrayList()
        val context = FxContext.current
        val displayItem = treeItem.value
        if (null != displayItem) {
            val modelType = displayItem.modelType.value
            if (ModelType.VARIABLE == modelType) {
                menuItems.add(createMenuItem(
                    context, Icon.VARIABLE, "edit-variable", treeItem
                ) { item: TreeItem<DisplayItem> -> editVariable(item) })
            }
            if (ModelType.MENU == modelType) {
                menuItems.add(createMenuItem(
                    context, Icon.MENU, "edit-menu", treeItem
                ) { item: TreeItem<DisplayItem> -> editMenu(item) })
                menuItems.add(SeparatorMenuItem())
                menuItems.add(createMenuItem(
                    context, Icon.VARIABLE, "create-variable", treeItem
                ) { item: TreeItem<DisplayItem> -> addVariable(item) })
                menuItems.add(createMenuItem(
                    context, Icon.MENU, "create-menu", treeItem
                ) { item: TreeItem<DisplayItem> -> addMenu(item) })
                menuItems.add(SeparatorMenuItem())
                menuItems.add(createMenuItem(
                    context, Icon.MENU, "split-menu", treeItem
                ) { item: TreeItem<DisplayItem> -> splitMenu(item) })
            }
            if (null != treeItem.parent) {
                menuItems.add(SeparatorMenuItem())
                menuItems.add(createMenuItem(
                    context, Icon.DELETE, "remove", treeItem
                ) { item: TreeItem<DisplayItem> -> removeItem(item) })
            }
            return ContextMenu(*menuItems.toTypedArray())
        }
        menuItems.add(SeparatorMenuItem())
        val rootItem = tree.root
        menuItems.add(createMenuItem(
            context, Icon.VARIABLE, "create-variable", rootItem
        ) { item: TreeItem<DisplayItem> -> addVariable(item) })
        menuItems.add(createMenuItem(
            context, Icon.MENU, "create-menu", rootItem
        ) { item: TreeItem<DisplayItem> -> addMenu(item) })
        return ContextMenu(*menuItems.toTypedArray())
    }

    private fun createDisplayItemTreeCell(): TreeCell<DisplayItem> {
        val treeCell: TreeCell<DisplayItem> =
            DisplayItemTreeCell { cell: TreeCell<DisplayItem>, displayItem: DisplayItem ->
                cell.text = displayItem.label.value.takeIf { it!!.isNotBlank() }
                    ?: displayItem.name.value
                displayItem.modelType.value
                    .let {
                        when (it) {
                            ModelType.MENU -> Icon.MENU
                            ModelType.VARIABLE -> Icon.VARIABLE
                            else -> Icon.EMPTY
                        }
                    }.findIcon(FxContext.current)?.toExternalForm()
                    ?.let { ImageView(it) }?.let { cell.setGraphic(it) }
                updateCell(cell, cell.treeItem, cell.index)
            }
        initializeDnD(treeCell)
        treeCell.indexProperty().addListener { _: ObservableValue<out Number>, _: Number, _: Number ->
            if (treeCell.treeItem != null) updateCell(treeCell, treeCell.treeItem, treeCell.index)
        }
        treeCell.treeViewProperty()
            .addListener { _: ObservableValue<out TreeView<DisplayItem>>, _: TreeView<DisplayItem>?, newValue: TreeView<DisplayItem> ->
                newValue.onScroll = EventHandler {
                    if (treeCell.treeItem != null) updateCell(treeCell, treeCell.treeItem, treeCell.index)
                }
            }
        treeCell.hoverProperty().addListener { _: ObservableValue<out Boolean>, _: Boolean, _: Boolean ->
            if (treeCell.treeItem != null) updateCell(treeCell, treeCell.treeItem, treeCell.index)
        }
        return treeCell
    }

    private fun createMenuItem(
        context: FxContext,
        icon: Icon,
        key: String,
        treeItem: TreeItem<DisplayItem>,
        handler: (TreeItem<DisplayItem>) -> Unit
    ): MenuItem {
        val item = MenuItem(resources[key], ImageView(icon.findIcon(context)?.toExternalForm()))
        item.onAction = EventHandler { handler(treeItem) }
        return item
    }

    private fun createPlaceHolder(resources: ResourceBundle): Node {
        val context = FxContext.current
        val placeholder = Button(
            resources[LC_TEMPLATE],
            ImageView(Icon.TEMPLATE.findIcon(context)?.toExternalForm())
        )
        placeholder.tooltip = Tooltip(resources[LC_TEMPLATE_TOOLTIP])
        placeholder.onAction = EventHandler { onTemplate() }
        return placeholder
    }

    private fun doTemplate() {
        if (FxContext.current.parameters.named[KEY_DEBUG] != null) {
            updateTree(ModelTemplate.getTestTemplateTree())
        } else {
            updateTree(ModelTemplate.getTemplateTree())
        }
    }

    private fun editMenu(item: TreeItem<DisplayItem>) {
        val dialog = MenuDialog()
        dialog.setKnown(getKnownNames((if (null == item.value) "" else item.value!!.name.value)))
        dialog.initOwner(stage)
        dialog.setDisplayItem(item.value)
        dialog.showAndWait().ifPresent { menu: DisplayItem -> replaceItem(item, menu) }
    }

    private fun editVariable(item: TreeItem<DisplayItem>) {
        with(VariableDialog()) {
            initOwner(stage)
            setDisplayItem(item.value)
            showAndWait()
        }.ifPresent { replaceItem(item, it) }
    }

    private fun getKnownNames(allow: String): Set<String> {
        val known: MutableSet<String> = mutableSetOf()
        visitItems(tree) {
            val name = it.name.value
            if (!name.equals(allow, ignoreCase = true)) {
                known.add(name)
            }
        }
        return known
    }

    private val root: TreeItem<DisplayItem>?
        get() = tree.root
    private val stage: Stage
        get() = findStage(component.view)!!

    private fun initializeDnD(cell: TreeCell<DisplayItem>) {
        cell.onDragDetected = EventHandler { event: MouseEvent ->
            if (!cell.isEmpty) {
                val dragboard = cell.startDragAndDrop(TransferMode.MOVE)
                dragboard.dragView = cell.snapshot(null, null)
                val content: MutableMap<DataFormat, Any> = ClipboardContent()
                content[SERIALIZED_MIME_TYPE] = cell.index
                dragboard.setContent(content)
                event.consume()
            }
        }
        cell.onDragOver = EventHandler { event: DragEvent ->
            val dragboard = event.dragboard
            if (dragboard.hasContent(SERIALIZED_MIME_TYPE)) {
                val dragItem = tree.getTreeItem((dragboard.getContent(SERIALIZED_MIME_TYPE) as Int))
                val index = tree.getRow(dragItem)
                val destItem = if (cell.isEmpty) tree.root else cell.treeItem
                if (cell.index != index) {
                    if (isNotParent(dragItem, destItem)) {
                        val dropping: DROPPING = DROPPING.determineDropping(cell, event, BOND)
                        if (canBeChildOfParent(dragItem.value, destItem.value) && DROPPING.ONTO == dropping) {
                            event.acceptTransferModes(TransferMode.MOVE)
                            event.consume()
                        } else if (DROPPING.ABOVE == dropping || DROPPING.BELOW == dropping) {
                            event.acceptTransferModes(TransferMode.MOVE)
                            event.consume()
                        }
                    }
                }
            }
        }
        cell.onDragDropped = EventHandler { event: DragEvent ->
            val dragboard = event.dragboard
            if (dragboard.hasContent(SERIALIZED_MIME_TYPE)) {
                val dragItem = tree.getTreeItem((dragboard.getContent(SERIALIZED_MIME_TYPE) as Int))
                val index = tree.getRow(dragItem)
                val destItem = if (cell.isEmpty) tree.root else cell.treeItem
                val cellIndex = cell.index
                if (cellIndex != index) {
                    if (isNotParent(dragItem, destItem)) {
                        val dropping: DROPPING = DROPPING.determineDropping(cell, event, BOND)
                        if (canBeChildOfParent(dragItem.value, destItem.value) && DROPPING.ONTO == dropping) {
                            attachTo(destItem, dragItem)
                            event.isDropCompleted = true
                            event.consume()
                            updateCell(cell, destItem, cellIndex)
                        } else if (DROPPING.ABOVE == dropping) {
                            putAbove(destItem, dragItem)
                            event.isDropCompleted = true
                            event.consume()
                            updateCell(cell, destItem, cellIndex)
                        } else if (DROPPING.BELOW == dropping) {
                            putBelow(destItem, dragItem)
                            event.isDropCompleted = true
                            event.consume()
                            updateCell(cell, destItem, cellIndex)
                        }
                    }
                }
            }
        }
    }

    private fun initializeTree(placeholder: Node) {
        tree.skin = TreeViewPlaceholderSkin(tree, changed, placeholder) { root?.children?.isEmpty() ?: true }
        shouldClearRoot()
        tree.isShowRoot = false
        tree.setCellFactory { createDisplayItemTreeCell() }
        scrollTimeline.cycleCount = Animation.INDEFINITE
        scrollTimeline.keyFrames.add(
            KeyFrame(Duration.millis(SCROLL.toDouble()), "Scroll", {
                tree.lookupAll(".scroll-bar")
                    .filterIsInstance<ScrollBar>()
                    .firstOrNull { Orientation.VERTICAL == it.orientation }
                    ?.let { it.value = 0.0.coerceAtLeast(1.0.coerceAtMost(it.value + scrollDirection)) }
            })
        )
        tree.onDragExited = EventHandler {
            scrollDirection = (if (0 < it.y) 1.0 / tree.expandedItemCount else -1.0 / tree.expandedItemCount)
            scrollTimeline.play()
        }
        tree.onDragEntered = EventHandler { scrollTimeline.stop() }
        tree.onDragDone = EventHandler { scrollTimeline.stop() }
    }

    private fun isDifferentTrees(oldRoot: TreeItem<DisplayItem>?, newRoot: TreeItem<DisplayItem>): Boolean {
        if (null == oldRoot) {
            return true
        }
        if (oldRoot !== newRoot) {
            val oldRootValue = oldRoot.value
            val newRootValue = newRoot.value
            if (oldRootValue != newRootValue) return true
            val oldRootChildren = oldRoot.children
            val newRootChildren = newRoot.children
            val size = oldRootChildren.size
            if (size != newRootChildren.size) return true
            for (i in 0 until size) {
                if (isDifferentTrees(oldRootChildren[i], newRootChildren[i])) return true
            }
        }
        return false
    }

    private fun onTemplate() {
        val context = FxContext.current
        if (isTreeEmpty || context.confirm(
                resources[LC_TEMPLATE_CONFIRM], stage,
                mapOf(
                    ButtonData.OK_DONE to resources[LC_TEMPLATE_CONFIRM_OK],
                    ButtonData.CANCEL_CLOSE to resources[LC_TEMPLATE_CONFIRM_CANCEL]
                )
            )
        ) {
            try {
                doTemplate()
            } catch (e: Exception) {
                if (ButtonData.OTHER == context.error(
                        stage, resources[LC_ERROR_MALFORMED_SCRIPT], e,
                        mapOf(
                            ButtonData.OK_DONE to resources[LC_CLOSE],
                            ButtonData.OTHER to resources[LC_REPORT]
                        )
                    )?.buttonData
                ) mailError(e)
            }
        }
    }

    private fun putAbove(destItem: TreeItem<DisplayItem>, dragItem: TreeItem<DisplayItem>) {
        val destSiblings = destItem.parent.children
        dragItem.parent.children.remove(dragItem)
        destSiblings.add(destSiblings.indexOf(destItem), dragItem)
        selectItem(dragItem)
    }

    private fun putBelow(destItem: TreeItem<DisplayItem>, dragItem: TreeItem<DisplayItem>) {
        val destSiblings = destItem.parent.children
        val index = destSiblings.indexOf(destItem)
        dragItem.parent.children.remove(dragItem)
        if (index < destSiblings.size - 1) {
            destSiblings.add(index + 1, dragItem)
        } else {
            destSiblings.add(dragItem)
        }
        selectItem(dragItem)
    }

    private fun removeItem(treeItem: TreeItem<DisplayItem>) {
        if (FxContext.current.confirm(
                resources[LC_REMOVE_CONFIRM], stage, mapOf(
                    ButtonData.OK_DONE to resources[LC_REMOVE_CONFIRM_OK],
                    ButtonData.CANCEL_CLOSE to resources[LC_REMOVE_CONFIRM_CANCEL]
                )
            )
        ) {
            treeItem.parent.children.remove(treeItem)
        }
    }

    private fun replaceItem(item: TreeItem<DisplayItem>?, displayItem: DisplayItem) {
        if (null != item) {
            item.value = displayItem
            changed.value = true
            selectItem(item)
        }
    }

    private fun selectItem(item: TreeItem<DisplayItem>) {
        var treeItem: TreeItem<DisplayItem>? = item
        while (null != treeItem) {
            treeItem.isExpanded = null != treeItem.value && ModelType.MENU === treeItem.value!!.modelType.value
            treeItem = treeItem.parent
        }
        tree.selectionModel.select(item)
        tree.scrollTo(tree.getRow(item))
    }

    private fun shouldClearRoot(): Boolean =
        shouldUpdateTree(DisplayItem.toTreeItem(DisplayItem.createRoot(), true))

    private fun shouldUpdateTree(root: TreeItem<DisplayItem>): Boolean =
        if (isDifferentTrees(tree.root, root)) {
            tree.root = root
            root.isExpanded = true
            selectItem(root)
            true
        } else false

    private fun splitMenu(item: TreeItem<DisplayItem>?) {
        val treeItem = item ?: tree.root
        val value = treeItem.value
        if (null != value) {
            val subMenus: MutableList<TreeItem<DisplayItem>> = FXCollections.observableArrayList()
            val children = treeItem.children
            val childrenCopy = FXCollections.observableArrayList(children)
            val childrenSize = childrenCopy.size
            val groups = 2.coerceAtLeast(childrenSize / FULLNESS.ALMOST.size)
            val label = value.label.value
            val name = value.name.value
            for (g in 0 until groups) {
                val num = g + 1
                subMenus += DisplayItem.toTreeItem(DisplayItem.createMenu("$label #$num", name + '_' + num), true)
            }
            val groupSize = ceil(childrenSize / groups.toDouble()).toInt()
            for (g in 0 until childrenSize) {
                val child = childrenCopy[g]
                children.remove(child)
                subMenus[g / groupSize].children.add(child)
            }
            children.setAll(subMenus)
            changed.value = true
            selectItem(treeItem)
        }
    }

    private fun updateCell(cell: TreeCell<DisplayItem>, treeItem: TreeItem<DisplayItem>, cellIndex: Int) {
        cell.contextMenu = createContextMenuForCell(treeItem)
        FxRun.runLater {
            setCellZebraDecorations(cell, cell.treeItem, cellIndex)
            setCellSignalDecorations(cell, cell.treeItem)
            setCellSplitDecorations(cell, cell.treeItem)
        }
    }

    private fun updateTree(root: ModelItem) {
        model = root
    }

    private class DisplayItemTreeCell(
        private val decorator: (TreeCell<DisplayItem>, DisplayItem) -> Unit
    ) : TreeCell<DisplayItem>() {
        override fun updateItem(item: DisplayItem?, empty: Boolean) {
            super.updateItem(item, empty)
            if (!empty && item != null) decorator(this, item)
        }
    }

    private class TreeViewPlaceholderSkin<T : Any>(
        treeView: TreeView<T>,
        private val watch: ObservableValue<*>,
        private val placeholder: Node,
        private val emptyPredicate: (TreeView<*>) -> Boolean
    ) : TreeViewSkin<T>(treeView) {
        private var placeholderRegion: StackPane? = null

        init {
            installPlaceholderSupport()
        }

        override fun layoutChildren(x: Double, y: Double, w: Double, h: Double) {
            super.layoutChildren(x, y, w, h)
            if (null != placeholderRegion && placeholderRegion!!.isVisible) {
                placeholderRegion!!.resizeRelocate(x, y, w, h)
            }
        }

        private fun installPlaceholderSupport() {
            registerChangeListener(watch) { updatePlaceholderSupport() }
            watch.addListener { _: ObservableValue<out Any>, _: Any, _: Any -> updatePlaceholderSupport() }
            updatePlaceholderSupport()
        }

        private val isTreeEmpty: Boolean
            get() = emptyPredicate(skinnable)

        private fun updatePlaceholderSupport() {
            if (isTreeEmpty) {
                if (null == placeholderRegion) {
                    placeholderRegion = StackPane()
                    placeholderRegion!!.styleClass.setAll(CLASS_PLACEHOLDER)
                    children.add(placeholderRegion)
                    placeholderRegion!!.children.setAll(placeholder)
                }
            }
            virtualFlow.isVisible = !isTreeEmpty
            placeholderRegion?.isVisible = isTreeEmpty
        }

        companion object {
            private const val CLASS_PLACEHOLDER = "placeholder"
        }
    }

    companion object {
        private const val BOND = 0.3
        private const val CLASS_DANGER = "danger"
        private const val CLASS_EVEN = "even"
        private const val CLASS_FIRST_CHILD = "first-child"
        private const val CLASS_LAST_CHILD = "last-child"
        private const val CLASS_MENU = "menu"
        private const val CLASS_ODD = "odd"
        private const val CLASS_VARIABLE = "variable"
        private const val CLASS_WARN = "warn"
        private const val KEY_DEBUG = "debug"
        private const val LC_CLOSE = "close"
        private const val LC_ERROR_MALFORMED_SCRIPT = "error.malformed-script"
        private const val LC_REMOVE_CONFIRM = "remove-confirm"
        private const val LC_REMOVE_CONFIRM_CANCEL = "remove-confirm-cancel"
        private const val LC_REMOVE_CONFIRM_OK = "remove-confirm-ok"
        private const val LC_REPORT = "report"
        private const val LC_TEMPLATE = "template"
        private const val LC_TEMPLATE_CONFIRM = "template-confirm"
        private const val LC_TEMPLATE_CONFIRM_CANCEL = "template-confirm-cancel"
        private const val LC_TEMPLATE_CONFIRM_OK = "template-confirm-ok"
        private const val LC_TEMPLATE_TOOLTIP = "template-tooltip"
        private const val SCROLL = 20
        private val SERIALIZED_MIME_TYPE = DataFormat("application/x-java-serialized-object")
        private fun canBeChildOfParent(child: DisplayItem, parent: DisplayItem): Boolean {
            child.modelType
            return ModelType.MENU == parent.modelType.value
        }

        @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
        private fun getFullness(treeItem: TreeItem<DisplayItem>): FULLNESS {
            return when (treeItem.value!!.modelType.value) {
                ModelType.VARIABLE -> FULLNESS.determineFullness(if (null == treeItem.parent) 0 else treeItem.parent.children.size)
                ModelType.MENU -> FULLNESS.determineFullness(treeItem.children.size)
            }
        }

        private fun isFirstChild(treeItem: TreeItem<DisplayItem>?): Boolean {
            if (null == treeItem) {
                return true
            }
            val parent = treeItem.parent ?: return true
            val children = parent.children
            return 0 == children.indexOf(treeItem)
        }

        private fun isLastChild(treeItem: TreeItem<DisplayItem>?): Boolean {
            if (null == treeItem) {
                return false
            }
            val parent = treeItem.parent ?: return false
            val children = parent.children
            return children.indexOf(treeItem) == children.size - 1
        }

        private fun isNotParent(parent: TreeItem<DisplayItem>, child: TreeItem<DisplayItem>): Boolean {
            var item: TreeItem<DisplayItem>? = child
            var result = true
            while (result && null != item) {
                result = item.parent !== parent
                item = item.parent
            }
            return result
        }

        private fun setCellSignalDecorations(cell: Styleable, treeItem: TreeItem<DisplayItem>?) {
            if (null != treeItem && null != treeItem.value) {
                when (getFullness(treeItem)) {
                    FULLNESS.NORMAL -> classRemoveAll(cell, CLASS_WARN, CLASS_DANGER)
                    FULLNESS.ALMOST -> classAddRemove(cell, CLASS_WARN, CLASS_DANGER)
                    FULLNESS.FULL -> classAddRemove(cell, CLASS_DANGER, CLASS_WARN)
                }
            } else {
                classRemoveAll(cell, CLASS_WARN, CLASS_DANGER)
            }
        }

        @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
        private fun setCellSplitDecorations(cell: Styleable, treeItem: TreeItem<DisplayItem>?) {
            classRemoveAll(cell, CLASS_VARIABLE, CLASS_MENU, CLASS_FIRST_CHILD, CLASS_LAST_CHILD)
            if (null != treeItem) {
                val displayItem = treeItem.value
                if (null != displayItem) {
                    when (displayItem.modelType.value) {
                        ModelType.MENU -> classAddRemoveAll(
                            cell, CLASS_MENU, CLASS_VARIABLE, CLASS_FIRST_CHILD, CLASS_LAST_CHILD
                        )
                        ModelType.VARIABLE -> {
                            classAddRemove(cell, CLASS_VARIABLE, CLASS_MENU)
                            if (isFirstChild(treeItem)) {
                                classAdd(cell, CLASS_FIRST_CHILD)
                            } else {
                                classRemove(cell, CLASS_FIRST_CHILD)
                            }
                            if (isLastChild(treeItem)) {
                                classAdd(cell, CLASS_LAST_CHILD)
                            } else {
                                classRemove(cell, CLASS_LAST_CHILD)
                            }
                        }
                    }
                }
            }
        }

        private fun setCellZebraDecorations(cell: Styleable, treeItem: TreeItem<DisplayItem>?, cellIndex: Int) {
            classRemoveAll(cell, CLASS_ODD, CLASS_EVEN)
            if (null != treeItem && null != treeItem.value) {
                if (0 == cellIndex % 2) {
                    classAddRemove(cell, CLASS_EVEN, CLASS_ODD)
                } else {
                    classAddRemove(cell, CLASS_ODD, CLASS_EVEN)
                }
            }
        }
    }
}
