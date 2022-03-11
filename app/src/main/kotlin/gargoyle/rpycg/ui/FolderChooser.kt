package gargoyle.rpycg.ui

import gargoyle.fx.FxComponent
import gargoyle.fx.FxContext
import gargoyle.fx.FxRun.runLater
import gargoyle.fx.log.FxLog
import gargoyle.rpycg.ex.AppUserException
import gargoyle.rpycg.ui.icons.Icon
import gargoyle.rpycg.util.GameUtil.isGameDirectory
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import javafx.scene.control.SelectionMode
import javafx.scene.control.Tooltip
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.image.ImageView
import javafx.util.Callback
import java.io.Closeable
import java.io.IOException
import java.net.InetAddress
import java.net.URL
import java.net.UnknownHostException
import java.nio.file.ClosedWatchServiceException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.WatchService
import java.util.LinkedList
import java.util.ResourceBundle
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors
import javax.swing.filechooser.FileSystemView

@Suppress("unused")
class FolderChooser : Dialog<Path>(), Initializable {
    private val additionalIconProvider: Property<(Path, Boolean) -> Node>

    @Suppress("UNNECESSARY_LATEINIT")
    private lateinit var component: FxComponent<FolderChooser, Parent>
    private val fileWatcher: FileWatcher
    private val initialDirectory: Property<Path>
    private val selectionFilter: Property<(Path) -> Boolean>

    @FXML
    private lateinit var fileTree: TreeView<Path>
    private lateinit var rootLabel: String

    init {
        fileWatcher = FileWatcher()
        initialDirectory = SimpleObjectProperty(null)
        selectionFilter = SimpleObjectProperty(null)
        additionalIconProvider = SimpleObjectProperty(null)
        component = FxContext.current.loadDialog(this) ?: throw AppUserException("No view {FolderChooser}")
    }

    fun additionalIconProviderProperty(): Property<(Path, Boolean) -> Node> =
        additionalIconProvider

    fun dispose() =
        fileWatcher.close()

    fun getAdditionalIconProvider(): (Path, Boolean) -> Node =
        additionalIconProvider.value

    fun setAdditionalIconProvider(additionalIconProvider: (Path, Boolean) -> Node) {
        this.additionalIconProvider.value = additionalIconProvider
    }

    fun getInitialDirectory(): Path? =
        initialDirectory.value

    fun setInitialDirectory(initialDirectory: Path?) {
        this.initialDirectory.value = initialDirectory
    }

    fun getSelectionFilter(): (Path) -> Boolean =
        selectionFilter.value

    fun setSelectionFilter(selectionFilter: (Path) -> Boolean) {
        this.selectionFilter.value = selectionFilter
    }

    override fun initialize(location: URL, resources: ResourceBundle) {
        FxContext.current.decorateDialog(
            this, {
                if (it?.buttonData?.isCancelButton == true) null
                else fileTree.selectionModel.selectedItem.value
            }, mapOf(
                ButtonData.OK_DONE to resources.getString(LC_OK),
                ButtonData.CANCEL_CLOSE to resources.getString(LC_CANCEL)
            ), resources.getString(LC_TITLE)
        )
        isResizable = true
        rootLabel = resources.getString(LC_ROOT)
        fileTree.cellFactory = Callback {
            FileTreeCell(rootLabel) { path: Path, expanded: Boolean ->
                getPathGraphic(path, expanded)
            }
        }
        fileTree.selectionModel.selectionMode = SelectionMode.SINGLE
        val rootNode = TreeItem<Path>(null, getPathGraphic(null, false))
        rootNode.children.setAll(findChildren(null, fileWatcher, { path: Path, expanded: Boolean ->
            getPathGraphic(path, expanded)
        }) { treeItem: TreeItem<Path>, _: Boolean -> scrollTo(treeItem) })
        rootNode.isExpanded = true
        fileTree.root = rootNode
        fileTree.selectionModel.selectedItemProperty()
            .addListener { _: ObservableValue<out TreeItem<Path>>, _: TreeItem<Path>?, newValue: TreeItem<Path> ->
                val selectionFilterValue = selectionFilter.value
                if (null != selectionFilterValue && null != newValue.value) {
                    dialogPane.buttonTypes.firstOrNull { buttonType: ButtonType -> buttonType.buttonData.isDefaultButton }
                        ?.let { dialogPane.lookupButton(it).isDisable = !selectionFilterValue(newValue.value!!) }
                }
            }
        initialDirectory.addListener { _: ObservableValue<out Path>, _: Path?, newValue: Path ->
            selectItem(newValue, !isGameDirectory(newValue))
        }
        runLater { fileTree.requestFocus() }
    }

    fun selectionFilterProperty(): Property<(Path) -> Boolean> =
        selectionFilter

    fun showDialog(): Path? {
        fileWatcher.start()
        val stage = component.primaryStage
        if (!stage.isShowing) initOwner(stage)
        updateItems(fileTree.selectionModel.selectedItem.value)
        val file = showAndWait()
        fileWatcher.close()
        file.ifPresent { initialDirectory.setValue(it) }
        return file.orElse(null)
    }

    override fun toString(): String =
        getPathText(null, false, rootLabel)

    private fun getExistingParent(curPath: Path): Path {
        var path = curPath
        while (!Files.isReadable(path)) {
            path = path.parent
        }
        return path
    }

    private fun getItems(
        root: TreeItem<Path>, result: MutableCollection<TreeItem<Path>>, targetPath: Path
    ): TreeItem<Path> {
        var item = root
        for (path in getParentPaths(targetPath)) {
            for (child in item.children) {
                if (path == child.value) {
                    item = child
                    result.add(item)
                }
            }
        }
        return item
    }

    private fun getParentPaths(curPath: Path): List<Path> {
        var path: Path? = curPath
        val paths: MutableList<Path> = ArrayList(path!!.nameCount)
        while (null != path) {
            paths.add(0, path)
            path = path.parent
        }
        return paths
    }

    private fun getPathGraphic(path: Path?, expanded: Boolean): Node {
        val icon = if (path == null) Icon.COMPUTER else {
            if (Files.isDirectory(path)) {
                if (expanded) Icon.FOLDER_OPEN else Icon.FOLDER
            } else Icon.FILE
        }
        val findIcon: URL = icon.findIcon(FxContext.current)!!
        return path?.let { additionalIconProvider.value?.invoke(it, expanded) } ?: ImageView(findIcon.toExternalForm())

    }

    private fun scrollTo(item: TreeItem<Path>) =
        fileTree.scrollTo(fileTree.getRow(item))

    private fun selectItem(path: Path, expanded: Boolean) {
        val result: MutableCollection<TreeItem<Path>> = LinkedList()
        val item = getItems(fileTree.root, result, getExistingParent(path))
        result.forEach {
            it.isExpanded = true
            fileTree.selectionModel.select(it)
        }
        item.isExpanded = expanded
        scrollTo(item)
    }

    private fun updateItems(path: Path) {
        val result: MutableCollection<TreeItem<Path>> = LinkedList()
        getItems(fileTree.root, result, path)
        result.forEach { treeItem: TreeItem<Path> ->
            val oldChildren = treeItem.children
            val newChildren = FXCollections.observableArrayList(oldChildren)
            newChildren.removeIf { !Files.isReadable(it.value) }
            findChildren(treeItem.value, fileWatcher, ::getPathGraphic)
            { item: TreeItem<Path>, _: Boolean -> scrollTo(item) }
                .filter { Files.isReadable(it.value) && !oldChildren.contains(it) }
                .forEach { newChildren.add(it) }
            oldChildren.setAll(newChildren)
        }
    }

    private class FileTreeCell(private val rootLabel: String, private val iconProvider: (Path, Boolean) -> Node?) :
        TreeCell<Path?>() {

        override fun updateItem(item: Path?, empty: Boolean) {
            super.updateItem(item, empty)
            if (empty || null == treeItem) {
                graphic = null
            } else {
                iconProvider(item!!, treeItem.isExpanded)?.let { graphic = it }
            }
            text = if (empty) null else getPathText(item, false, rootLabel)
            tooltip = if (empty) null else Tooltip(getPathText(item, true, rootLabel))
        }
    }

    private class FileTreeItem(
        path: Path?,
        private val fileWatcher: FileWatcher,
        private val iconProvider: (Path, Boolean) -> Node,
        private val navigate: (TreeItem<Path>, Boolean) -> Unit
    ) : TreeItem<Path>(path) {
        private var fetch = false

        init {
            if (null != path) {
                value = path
                if (Files.isDirectory(path)) {
                    addEventHandler(branchCollapsedEvent()) { e: TreeModificationEvent<Path> ->
                        val treeItem = e.source
                        treeItem.graphic = iconProvider(treeItem.value!!, e.source.isExpanded)
                        treeItem.children.clear()
                        runLater { navigate(treeItem, false) }
                    }
                    addEventHandler(branchExpandedEvent()) { e: TreeModificationEvent<Path> ->
                        val treeItem = e.source
                        updateChildren(treeItem)
                        iconProvider(treeItem.value!!, treeItem.isExpanded).also { treeItem.setGraphic(it) }
                        fetch = false
                        runLater { navigate(treeItem!!, true) }
                    }
                    fileWatcher.register(value) { updateChildren(this) }
                }
                graphic = iconProvider(path, false)
                parentProperty().addListener { _: ObservableValue<out TreeItem<Path>>, _: TreeItem<Path>?, newValue: TreeItem<Path>? ->
                    if (null == newValue) {
                        fetch = false
                        fileWatcher.unregister(path)
                    }
                }
            }
        }

        override fun hashCode(): Int =
            value.hashCode()

        override fun equals(other: Any?): Boolean =
            other is TreeItem<*> && value == other.value

        override fun isLeaf(): Boolean =
            Files.isRegularFile(value)

        override fun getChildren(): ObservableList<TreeItem<Path>> {
            val oldChildren = super.getChildren()
            if (fetch) {
                return oldChildren
            }
            fetch = true
            val newChildren = findNewChildren(this, oldChildren, navigate)
            oldChildren.setAll(newChildren)
            return newChildren
        }

        private fun findNewChildren(
            treeItem: TreeItem<Path>,
            oldChildren: ObservableList<TreeItem<Path>>,
            collapse: (TreeItem<Path>, Boolean) -> Unit
        ): ObservableList<TreeItem<Path>> {
            val newChildren = FXCollections.observableArrayList(oldChildren)
            newChildren.removeIf { !Files.isReadable(it.value!!) }
            findChildren(treeItem.value, fileWatcher, iconProvider, collapse)
                .filter { Files.isReadable(it.value!!) && !oldChildren.contains(it) && it.value!!.parent == treeItem.value }
                .forEach { newChildren.add(it) }
            return newChildren
        }

        private fun updateChildren(treeItem: TreeItem<Path>): Boolean {
            val oldChildren = treeItem.children
            fetch = true
            val newChildren = findNewChildren(treeItem, oldChildren, navigate)
            oldChildren.setAll(newChildren)
            return fetch
        }
    }

    private class FileWatcher : Closeable {
        private val watch: MutableMap<Path, FileWatcherData> = ConcurrentHashMap(2)
        private var watchService: WatchService = try {
            FileSystems.getDefault().newWatchService()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        @Volatile
        private var running = false

        override fun close() {
            running = false
            try {
                watch.forEach { (key: Path, _: FileWatcherData?) -> unregister(key) }
                watch.clear()
                watchService.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        private fun createThread(): Thread {
            return Thread({
                running = true
                while (running) {
                    try {
                        Thread.sleep(100)
                    } catch (e: InterruptedException) {
                        FxLog.error(e, "interrupted")
                    }
                    watch.forEach { (path, data) ->
                        val dataPath: Path = data.path
                        try {
                            var key: WatchKey
                            val watchPaths: MutableList<FileWatcherEventData> = LinkedList()
                            while (null != watchService.poll().also { key = it }) {
                                for (watchEvent in key.pollEvents()) {
                                    if (dataPath == path) {
                                        watchPaths.add(
                                            FileWatcherEventData(
                                                dataPath.resolve(watchEvent.context() as Path),
                                                watchEvent.kind(),
                                                watchEvent.count()
                                            )
                                        )
                                    }
                                }
                                key.reset()
                            }
                            if (watchPaths.isNotEmpty()) {
                                data.callback(FileWatcherEvent(path, watchPaths))
                            }
                        } catch (e: ClosedWatchServiceException) {
                            watch.remove(path)
                        }
                    }
                }
            }, FileWatcher::class.qualifiedName!!)
        }

        fun register(path: Path?, callback: (FileWatcherEvent) -> Boolean) {
            if (null != path && Files.isReadable(path) && !watch.containsKey(path)) {
                try {
                    path.register(
                        watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY
                    )
                    watch[path] = FileWatcherData(path, callback)
                } catch (e: IOException) {
                    FxLog.error(e, "register")
                }
            }
        }

        fun start() {
            val thread = createThread()
            thread.isDaemon = true
            thread.start()
        }

        fun unregister(path: Path) {
            watch.remove(path)
        }
    }

    private class FileWatcherData(val path: Path, val callback: (FileWatcherEvent) -> Boolean)
    private class FileWatcherEvent(
        private val registeredPath: Path, private val watcherEventData: List<FileWatcherEventData>
    )

    private class FileWatcherEventData(
        private val path: Path, private val kind: WatchEvent.Kind<*>, private val count: Int
    )

    companion object {
        private const val LC_CANCEL = "cancel"
        private const val LC_OK = "ok"
        private const val LC_ROOT = "root"
        private const val LC_TITLE = "title"
        private fun findChildren(
            path: Path?,
            fileWatcher: FileWatcher,
            iconProvider: (Path, Boolean) -> Node,
            navigate: (TreeItem<Path>, Boolean) -> Unit
        ): ObservableList<TreeItem<Path>> =
            if (null == path) {
                FXCollections.observableArrayList(FileSystems.getDefault().rootDirectories
                    .filter { Files.isDirectory(it) }
                    .sorted()
                    .map { FileTreeItem(it, fileWatcher, iconProvider, navigate) }
                    .toList())
            } else if (Files.isDirectory(path)) {
                try {
                    Files.list(path).collect(Collectors.toList()).let { list ->
                        FXCollections.observableArrayList(list.filter { Files.isDirectory(it) }
                            .sorted()
                            .map { FileTreeItem(it, fileWatcher, iconProvider, navigate) }
                            .toList())
                    }
                } catch (e: IOException) {
                    FXCollections.observableArrayList()
                }
            } else FXCollections.observableArrayList()

        private fun getPathText(path: Path?, full: Boolean, rootLabel: String): String {
            if (null != path) {
                if (full || null == path.fileName) {
                    try {
                        if (Files.isSameFile(path, path.root)) {
                            return FileSystemView.getFileSystemView().getSystemDisplayName(path.toFile())
                        }
                    } catch (e: IOException) {
                        return path.toString()
                    }
                    return path.toString()
                }
                return path.fileName.toString()
            }
            return try {
                InetAddress.getLocalHost().hostName
            } catch (x: UnknownHostException) {
                rootLabel
            }
        }
    }
}
