package gargoyle.rpycg.ui

import gargoyle.fx.FxCloseAction
import gargoyle.fx.FxComponent
import gargoyle.fx.FxContext
import gargoyle.fx.FxLauncher
import gargoyle.fx.FxRun.runLater
import gargoyle.fx.FxUtil.get
import gargoyle.rpycg.ex.AppException
import gargoyle.rpycg.ex.CodeGenerationException
import gargoyle.rpycg.ex.MalformedScriptException
import gargoyle.rpycg.model.ModelItem
import gargoyle.rpycg.service.CodeConverter
import gargoyle.rpycg.service.ErrorMailer.mailError
import gargoyle.rpycg.service.ScriptConverter
import gargoyle.rpycg.service.Storage
import gargoyle.rpycg.ui.icons.Icon
import gargoyle.rpycg.util.GameUtil.isGameDirectory
import javafx.beans.binding.Bindings
import javafx.beans.value.ObservableObjectValue
import javafx.beans.value.ObservableValue
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.control.MenuButton
import javafx.scene.control.MenuItem
import javafx.scene.control.Tab
import javafx.scene.image.ImageView
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.layout.BorderPane
import javafx.stage.FileChooser
import java.io.Closeable
import java.net.URL
import java.nio.file.Path
import java.util.ResourceBundle
import kotlin.io.path.exists
import kotlin.io.path.writeText

class Main : BorderPane(), Initializable, Closeable {
    @Suppress("JoinDeclarationAndAssignment", "UNNECESSARY_LATEINIT")
    private lateinit var component: FxComponent<Main, Main>

    @FXML
    private lateinit var btnLoad: MenuItem

    @FXML
    private lateinit var btnLoadReload: MenuButton

    @FXML
    private lateinit var btnReinstall: MenuItem

    @FXML
    private lateinit var btnReload: MenuItem

    @FXML
    private lateinit var btnSave: MenuItem

    @FXML
    private lateinit var btnSaveAs: MenuItem

    @FXML
    private lateinit var btnSaveSaveAs: MenuButton

    @FXML
    private lateinit var builder: Builder
    private lateinit var codeConverter: CodeConverter

    @FXML
    private lateinit var creator: Creator
    private lateinit var gameChooser: FolderChooser
    private lateinit var resources: ResourceBundle
    private lateinit var scriptConverter: ScriptConverter
    private lateinit var storage: Storage
    private lateinit var storageChooser: FileChooser

    @FXML
    private lateinit var tabBuilder: Tab

    @FXML
    private lateinit var tabCreator: Tab

    @FXML
    private lateinit var tabSettings: TabSettings

    init {
        component = FxContext.current.loadComponent(this) ?: error("No view {Main}")
    }

    override fun close(): Unit =
        gameChooser.dispose()

    override fun initialize(location: URL, resources: ResourceBundle) {
        this.resources = resources
        scriptConverter = ScriptConverter()
        codeConverter = CodeConverter(FxContext.current, tabSettings.settings)
        gameChooser = createGameChooser(resources, tabSettings.gameDirectory)
        initializeTabs()
        storage = createStorage()
        storageChooser = createStorageChooser(storage.getPath(), tabSettings.storageDirectory)
        runLater { FxLauncher.requestPrevent(FxContext.current) { doSaveOnClose(resources) } }
    }

    @Suppress("UNUSED_PARAMETER")
    @FXML
    fun onClear(actionEvent: ActionEvent) {
        if (builder.isTreeEmpty || FxContext.current.confirm(
                resources[LC_CLEAR_CONFIRM], mapOf(
                    ButtonData.OK_DONE to resources[LC_CLEAR_CONFIRM_OK],
                    ButtonData.CANCEL_CLOSE to resources[LC_CLEAR_CONFIRM_CANCEL]
                )
            )
        ) {
            doClear()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @FXML
    fun onGenerate(actionEvent: ActionEvent) {
        val context = FxContext.current
        try {
            putClipboard(generateCodeString())
            context.alert(resources[LC_SUCCESS_GENERATE])
        } catch (e: CodeGenerationException) {
            context.error("${resources[LC_ERROR_GENERATE]}\n${e.localizedMessage}")
        } catch (e: RuntimeException) {
            context.error(
                resources[LC_ERROR_GENERATE], e, mapOf(
                    ButtonData.OK_DONE to resources[LC_CLOSE],
                    ButtonData.OTHER to resources[LC_REPORT]
                )
            )?.let { ButtonData.OTHER == it.buttonData }?.also { mailError(e) }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @FXML
    fun onInstall(actionEvent: ActionEvent) {
        chooseGameDirectory()?.let { doInstall(it) }
    }

    @Suppress("UNUSED_PARAMETER")
    @FXML
    fun onLoad(actionEvent: ActionEvent) {
        storageChooser.showOpenDialog(component.stage)?.toPath()?.let { path: Path ->
            val context = FxContext.current
            if (builder.isTreeEmpty || context.confirm(
                    resources[LC_LOAD_CONFIRM], mapOf(
                        ButtonData.OK_DONE to resources[LC_LOAD_CONFIRM_OK],
                        ButtonData.CANCEL_CLOSE to resources[LC_LOAD_CONFIRM_CANCEL]
                    )
                )
            ) {
                try {
                    doLoad(path)
                } catch (e: MalformedScriptException) {
                    context.error("${resources[LC_ERROR_MALFORMED_SCRIPT]}\n${e.localizedMessage}")
                } catch (e: RuntimeException) {
                    context.error(
                        resources[LC_ERROR_LOAD], e, mapOf(
                            ButtonData.OK_DONE to resources[LC_CLOSE],
                            ButtonData.OTHER to resources[LC_REPORT]
                        )
                    )?.let { ButtonData.OTHER == it.buttonData }.let { mailError(e) }
                }
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @FXML
    fun onMenu(actionEvent: ActionEvent): Unit =
        builder.addRootMenu()

    @Suppress("UNUSED_PARAMETER")
    @FXML
    fun onReinstall(actionEvent: ActionEvent): Unit =
        doInstall((storage.getGamePath()))

    @Suppress("UNUSED_PARAMETER")
    @FXML
    fun onReload(actionEvent: ActionEvent) {
        storage.getPath()?.let { path: Path ->
            val context = FxContext.current
            if (builder.isTreeEmpty || context.confirm(
                    resources[LC_RELOAD_CONFIRM], mapOf(
                        ButtonData.OK_DONE to resources[LC_RELOAD_CONFIRM_OK],
                        ButtonData.CANCEL_CLOSE to resources[LC_RELOAD_CONFIRM_CANCEL]
                    )
                )
            ) {
                try {
                    doLoad(path)
                } catch (e: MalformedScriptException) {
                    context.error("${resources[LC_ERROR_MALFORMED_SCRIPT]}\n${e.localizedMessage}")
                } catch (e: RuntimeException) {
                    context.error(
                        resources[LC_ERROR_LOAD], e, mapOf(
                            ButtonData.OK_DONE to resources[LC_CLOSE],
                            ButtonData.OTHER to resources[LC_REPORT]
                        )
                    )?.let { ButtonData.OTHER == it.buttonData }.let { mailError(e) }
                }
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @FXML
    fun onSave(actionEvent: ActionEvent) {
        storage.getPath().let {
            try {
                doSave()
            } catch (e: RuntimeException) {
                FxContext.current.error(
                    resources[LC_ERROR_SAVE], e, mapOf(
                        ButtonData.OK_DONE to resources[LC_CLOSE],
                        ButtonData.OTHER to resources[LC_REPORT]
                    )
                )?.let { ButtonData.OTHER == it.buttonData }.let { mailError(e) }
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @FXML
    fun onSaveAs(actionEvent: ActionEvent) {
        try {
            doSaveAs()
        } catch (e: RuntimeException) {
            FxContext.current.error(
                resources[LC_ERROR_SAVE], e, mapOf(
                    ButtonData.OK_DONE to resources[LC_CLOSE],
                    ButtonData.OTHER to resources[LC_REPORT]
                )
            )?.let { ButtonData.OTHER == it.buttonData }.let { mailError(e) }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @FXML
    fun onVariable(actionEvent: ActionEvent): Unit =
        builder.addRootVariable()

    private fun chooseGameDirectory(): Path? {
        if (null == gameChooser.owner) gameChooser.initOwner(component.stage)
        gameChooser.getInitialDirectory()?.let {
            var initialDirectory = it
            while (!initialDirectory.exists()) {
                initialDirectory = initialDirectory.parent
                gameChooser.setInitialDirectory(initialDirectory)
            }
        }
        return (gameChooser.showDialog())
    }

    private fun createStorage(): Storage =
        with(Storage()) {
            Bindings.isNull(pathProperty() as ObservableObjectValue<*>).let {
                btnReload.disableProperty().bind(it)
                btnSave.disableProperty().bind(it)
            }
            btnReinstall.disableProperty().bind(Bindings.isNull(gamePathProperty() as ObservableObjectValue<*>))
            this
        }

    private fun createStorageChooser(storagePath: Path?, storageDirectory: Path?): FileChooser =
        with(FileChooser()) {
            FileChooser.ExtensionFilter(resources[LC_EXTENSION_DESCRIPTION], "*.$EXTENSION")
                .let {
                    extensionFilters.add(it)
                    selectedExtensionFilter = it
                }
            storagePath?.let {
                initialDirectory = it.parent.toFile()
                initialFileName = it.fileName.toString()
            } ?: storageDirectory?.let { initialDirectory = it.toFile() }
            this
        }

    private fun doClear() {
        builder.clearAll()
        updateScript(true)
    }

    private fun doInstall(gamePath: Path) {
        val context = FxContext.current
        if (!isGameDirectory(gamePath)) {
            context.error(resources[LC_ERROR_NOT_GAME])
            return
        }
        try {
            gamePath.resolve("game").resolve(INSTALL_NAME).writeText(generateCodeString())
            context.alert(resources[LC_SUCCESS_INSTALL])
            storeGamePath(gamePath)
        } catch (e: CodeGenerationException) {
            context.error(
                resources[LC_ERROR_WRITE], e, mapOf(
                    ButtonData.OK_DONE to resources[LC_CLOSE],
                    ButtonData.OTHER to resources[LC_REPORT]
                )
            )?.let { ButtonData.OTHER == it.buttonData }.let { mailError(e) }
        } catch (e: Exception) {
            context.error(
                resources[LC_ERROR_WRITE], e, mapOf(
                    ButtonData.OK_DONE to resources[LC_CLOSE],
                    ButtonData.OTHER to resources[LC_REPORT]
                )
            )?.let { ButtonData.OTHER == it.buttonData }.let { mailError(e) }
        }
    }

    private fun doLoad(path: Path) {
        storage.setPath(path)
        tabSettings.storageDirectory = (path.parent)
        updateTree(storage.load(path))
        updateScript(true)
        storage.setModified(false)
    }

    private fun doSave(): Boolean =
        storage.getPath()?.let {
            if (it.exists() || FxContext.current.confirm(
                    resources[LC_SAVE_CONFIRM], mapOf(
                        ButtonData.OK_DONE to resources[LC_SAVE_CONFIRM_OK],
                        ButtonData.CANCEL_CLOSE to resources[LC_SAVE_CONFIRM_CANCEL]
                    )
                )
            ) {
                save(it)
            } else false
        } ?: false

    private fun doSaveAs(): Boolean =
        storageChooser.showSaveDialog(component.stage)?.toPath()?.let {
            if (!it.exists() || FxContext.current.confirm(
                    resources[LC_SAVE_AS_CONFIRM], mapOf(
                        ButtonData.OK_DONE to resources[LC_SAVE_AS_CONFIRM_OK],
                        ButtonData.CANCEL_CLOSE to resources[LC_SAVE_AS_CONFIRM_CANCEL]
                    )
                )
            ) {
                save(it)
                tabSettings.storageDirectory = it.parent
            }
            true
        } ?: false

    private fun doSaveOnClose(resources: ResourceBundle): FxCloseAction {
        val context = FxContext.current
        return if (!storage.getModified() || builder.isTreeEmpty || context.confirm(
                resources[LC_CLOSE_CONFIRM], mapOf(
                    ButtonData.OK_DONE to resources[LC_CLOSE_CONFIRM_OK],
                    ButtonData.CANCEL_CLOSE to resources[LC_CLOSE_CONFIRM_CANCEL]
                )
            )
        ) FxCloseAction.CLOSE else try {
            val storagePath = storage.getPath()
            if (null != storagePath) {
                if (doSave()) FxCloseAction.CLOSE else FxCloseAction.KEEP
            } else {
                if (doSaveAs()) FxCloseAction.CLOSE else FxCloseAction.KEEP
            }
        } catch (e: IllegalArgumentException) {
            context.error(
                resources[LC_ERROR_MALFORMED_SCRIPT], e, mapOf(
                    ButtonData.OK_DONE to resources[LC_CLOSE],
                    ButtonData.OTHER to resources[LC_REPORT]
                )
            )?.let { ButtonData.OTHER == it.buttonData }.let { mailError(e) }
            FxCloseAction.KEEP
        } catch (e: IllegalStateException) {
            context.error(
                resources[LC_ERROR_MALFORMED_SCRIPT], e, mapOf(
                    ButtonData.OK_DONE to resources[LC_CLOSE],
                    ButtonData.OTHER to resources[LC_REPORT]
                )
            )?.let { ButtonData.OTHER == it.buttonData }.let { mailError(e) }
            FxCloseAction.KEEP
        }
    }

    private fun generateCode(): List<String> =
        codeConverter.toCode(builder.model)

    private fun generateCodeString(): String =
        generateCode().joinToString(System.lineSeparator())

    private fun initializeTabs() {
        tabCreator.selectedProperty().addListener { _: ObservableValue<out Boolean>, _: Boolean, newValue: Boolean ->
            if (newValue) {
                updateScript(true)
                runLater { creator.onShow() }
            }
        }
        tabBuilder.selectedProperty().addListener { _: ObservableValue<out Boolean>, _: Boolean, newValue: Boolean ->
            if (newValue) {
                try {
                    updateTreeFromScript()
                    creator.decorateError(emptySet())
                } catch (e: IllegalArgumentException) {
                    creator.decorateError(setOf(e.localizedMessage))
                    FxContext.current.error("${resources.getString(LC_ERROR_MALFORMED_SCRIPT)}\n${e.localizedMessage}")
                } catch (e: IllegalStateException) {
                    creator.decorateError(setOf(e.localizedMessage))
                    FxContext.current.error("${resources.getString(LC_ERROR_MALFORMED_SCRIPT)}\n${e.localizedMessage}")
                } catch (e: MalformedScriptException) {
                    creator.decorateError(setOf(e.localizedMessage))
                    FxContext.current.error("${resources.getString(LC_ERROR_MALFORMED_SCRIPT)}\n${e.localizedMessage}")
                }
            }
        }
        builder.changedProperty().addListener { _: ObservableValue<out Boolean>, _: Boolean, newValue: Boolean ->
            if (newValue) {
                updateScript(false)
                storage.setModified(true)
            }
            builder.setChanged(false)
        }
        creator.changedProperty().addListener { _: ObservableValue<out Boolean>, _: Boolean, newValue: Boolean ->
            if (newValue) {
                try {
                    updateTreeFromScript()
                    creator.decorateError(emptySet())
                    storage.setModified(true)
                } catch (e: Exception) {
                    creator.decorateError(setOf(e.localizedMessage))
                }
            }
            creator.setChanged(false)
        }
    }

    private fun save(storagePath: Path): Boolean =
        try {
            storage.saveAs(storagePath, builder.model)
            storage.setPath(storagePath)
            true
        } catch (e: AppException) {
            FxContext.current.error(
                resources[LC_ERROR_SAVE], e, mapOf(
                    ButtonData.OK_DONE to resources[LC_CLOSE],
                    ButtonData.OTHER to resources[LC_REPORT]
                )
            )?.let { ButtonData.OTHER == it.buttonData }.let { mailError(e) }
            false
        }

    private fun storeGamePath(gamePath: Path) {
        tabSettings.gameDirectory = gamePath
        gameChooser.setInitialDirectory(gamePath)
        storage.setGamePath(gamePath)
    }

    private fun updateScript(forced: Boolean) {
        val script = scriptConverter.toScript(builder.model)
        if (forced) {
            creator.script = script
        } else {
            creator.setScriptUnforced(script)
        }
    }

    private fun updateTree(root: ModelItem) {
        builder.model = root
    }

    private fun updateTreeFromScript() {
        builder.model = scriptConverter.fromScript(creator.script)
    }

    companion object {
        private const val EXTENSION = "rpycg"
        private const val INSTALL_NAME = "RenPyCheat.rpy"
        private const val LC_CLEAR_CONFIRM = "clear-confirm"
        private const val LC_CLEAR_CONFIRM_CANCEL = "clear-confirm-cancel"
        private const val LC_CLEAR_CONFIRM_OK = "clear-confirm-ok"
        private const val LC_CLOSE = "close"
        private const val LC_CLOSE_CONFIRM = "close-confirm"
        private const val LC_CLOSE_CONFIRM_CANCEL = "close-confirm-cancel"
        private const val LC_CLOSE_CONFIRM_OK = "close-confirm-ok"
        private const val LC_ERROR_GENERATE = "error.generate"
        private const val LC_ERROR_LOAD = "error.load"
        private const val LC_ERROR_MALFORMED_SCRIPT = "error.malformed-script"
        private const val LC_ERROR_NOT_GAME = "error.not-game"
        private const val LC_ERROR_SAVE = "error.save"
        private const val LC_ERROR_WRITE = "error.write"
        private const val LC_EXTENSION_DESCRIPTION = "extension-description"
        private const val LC_GAME_CHOOSER_TITLE = "game-chooser-title"
        private const val LC_LOAD_CONFIRM = "load-confirm"
        private const val LC_LOAD_CONFIRM_CANCEL = "load-confirm-cancel"
        private const val LC_LOAD_CONFIRM_OK = "load-confirm-ok"
        private const val LC_RELOAD_CONFIRM = "reload-confirm"
        private const val LC_RELOAD_CONFIRM_CANCEL = "reload-confirm-cancel"
        private const val LC_RELOAD_CONFIRM_OK = "reload-confirm-ok"
        private const val LC_REPORT = "report"
        private const val LC_SAVE_AS_CONFIRM = "save-as-confirm"
        private const val LC_SAVE_AS_CONFIRM_CANCEL = "save-as-confirm-cancel"
        private const val LC_SAVE_AS_CONFIRM_OK = "save-as-confirm-ok"
        private const val LC_SAVE_CONFIRM = "save-confirm"
        private const val LC_SAVE_CONFIRM_CANCEL = "save-confirm-cancel"
        private const val LC_SAVE_CONFIRM_OK = "save-confirm-ok"
        private const val LC_SUCCESS_GENERATE = "success-generate"
        private const val LC_SUCCESS_INSTALL = "success-install"

        private fun createGameChooser(resources: ResourceBundle, gameDirectory: Path?): FolderChooser =
            with(FolderChooser()) {
                title = resources.getString(LC_GAME_CHOOSER_TITLE)
                setInitialDirectory(gameDirectory)
                setSelectionFilter { isGameDirectory(it) }
                setAdditionalIconProvider { path: Path?, expanded: Boolean ->
                    ImageView((path?.let {
                        (if (isGameDirectory(it)) {
                            if (expanded) Icon.GAME_FOLDER_OPEN else Icon.GAME_FOLDER
                        } else {
                            if (expanded) Icon.FOLDER_OPEN else Icon.FOLDER
                        })
                    } ?: Icon.EMPTY).findIcon(FxContext.current)!!.toExternalForm())
                }
                this
            }

        private fun putClipboard(content: String) {
            Clipboard.getSystemClipboard().setContent(ClipboardContent().also { it.putString(content) })
        }
    }
}
