package gargoyle.rpycg.ui

import gargoyle.fx.FxCloseAction
import gargoyle.fx.FxComponent
import gargoyle.fx.FxContext
import gargoyle.fx.FxLauncher.requestPrevent
import gargoyle.fx.FxRun.runLater
import gargoyle.rpycg.ex.AppException
import gargoyle.rpycg.ex.AppUserException
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
import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.util.ResourceBundle

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
    private var codeConverter: CodeConverter? = null

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
        component = FxContext.current.loadComponent(this) ?: throw  AppUserException("No view {Main}")
    }

    override fun close() {
        gameChooser.dispose()
    }

    override fun initialize(location: URL, resources: ResourceBundle) {
        this.resources = resources
        scriptConverter = ScriptConverter()
        codeConverter = CodeConverter(FxContext.current, tabSettings.settings)
        gameChooser = createGameChooser(resources, tabSettings.gameDirectory)
        initializeTabs()
        storage = createStorage()
        storageChooser = createStorageChooser(storage.getPath(), tabSettings.storageDirectory)
        runLater {
            requestPrevent(
                FxContext.current
            ) { doSaveOnClose(resources) }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @FXML
    fun onClear(actionEvent: ActionEvent) {
        if (builder.isTreeEmpty || FxContext.current.confirm(
                resources.getString(LC_CLEAR_CONFIRM), mapOf(
                    ButtonData.OK_DONE to resources.getString(LC_CLEAR_CONFIRM_OK),
                    ButtonData.CANCEL_CLOSE to resources.getString(LC_CLEAR_CONFIRM_CANCEL)
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
            context.alert(resources.getString(LC_SUCCESS_GENERATE))
        } catch (e: CodeGenerationException) {
            context.error(
                """
    ${resources.getString(LC_ERROR_GENERATE)}
    ${e.localizedMessage}
    """.trimIndent()
            )
        } catch (e: RuntimeException) {
            context.error(
                resources.getString(LC_ERROR_GENERATE), e, mapOf(
                    ButtonData.OK_DONE to resources.getString(LC_CLOSE),
                    ButtonData.OTHER to resources.getString(LC_REPORT)
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
        (storageChooser.showOpenDialog(component.stage))?.toPath()?.let { path: Path ->
            val context = FxContext.current
            if (builder.isTreeEmpty || context.confirm(
                    resources.getString(LC_LOAD_CONFIRM), mapOf(
                        ButtonData.OK_DONE to resources.getString(LC_LOAD_CONFIRM_OK),
                        ButtonData.CANCEL_CLOSE to resources.getString(LC_LOAD_CONFIRM_CANCEL)
                    )
                )
            ) {
                try {
                    doLoad(path)
                } catch (e: MalformedScriptException) {
                    context.error(
                        """
    ${resources.getString(LC_ERROR_MALFORMED_SCRIPT)}
    ${e.localizedMessage}
    """.trimIndent()
                    )
                } catch (e: RuntimeException) {
                    context.error(
                        resources.getString(LC_ERROR_LOAD), e, mapOf(
                            ButtonData.OK_DONE to resources.getString(LC_CLOSE),
                            ButtonData.OTHER to resources.getString(LC_REPORT)
                        )
                    )?.let { ButtonData.OTHER == it.buttonData }.let { mailError(e) }
                }
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @FXML
    fun onMenu(actionEvent: ActionEvent) {
        builder.addRootMenu()
    }

    @Suppress("UNUSED_PARAMETER")
    @FXML
    fun onReinstall(actionEvent: ActionEvent) {
        doInstall((storage.getGamePath()))
    }

    @Suppress("UNUSED_PARAMETER")
    @FXML
    fun onReload(actionEvent: ActionEvent) {
        (storage.getPath())?.let { path: Path ->
            val context = FxContext.current
            if (builder.isTreeEmpty || context.confirm(
                    resources.getString(LC_RELOAD_CONFIRM), mapOf(
                        ButtonData.OK_DONE to resources.getString(LC_RELOAD_CONFIRM_OK),
                        ButtonData.CANCEL_CLOSE to resources.getString(LC_RELOAD_CONFIRM_CANCEL)
                    )
                )
            ) {
                try {
                    doLoad(path)
                } catch (e: MalformedScriptException) {
                    context.error(
                        """
        ${resources.getString(LC_ERROR_MALFORMED_SCRIPT)}
        ${e.localizedMessage}
        """.trimIndent()
                    )
                } catch (e: RuntimeException) {
                    context.error(
                        resources.getString(LC_ERROR_LOAD), e, mapOf(
                            ButtonData.OK_DONE to resources.getString(LC_CLOSE),
                            ButtonData.OTHER to resources.getString(LC_REPORT)
                        )
                    )?.let { ButtonData.OTHER == it.buttonData }.let { mailError(e) }
                }
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @FXML
    fun onSave(actionEvent: ActionEvent) {
        (storage.getPath()).let {
            try {
                doSave()
            } catch (e: RuntimeException) {
                FxContext.current.error(
                    resources.getString(LC_ERROR_SAVE), e, mapOf(
                        ButtonData.OK_DONE to resources.getString(LC_CLOSE),
                        ButtonData.OTHER to resources.getString(LC_REPORT)
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
                resources.getString(LC_ERROR_SAVE), e, mapOf(
                    ButtonData.OK_DONE to resources.getString(LC_CLOSE),
                    ButtonData.OTHER to resources.getString(LC_REPORT)
                )
            )?.let { ButtonData.OTHER == it.buttonData }.let { mailError(e) }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @FXML
    fun onVariable(actionEvent: ActionEvent) {
        builder.addRootVariable()
    }

    private fun chooseGameDirectory(): Path? {
        if (null == gameChooser.owner) {
            gameChooser.initOwner(component.stage)
        }
        (gameChooser.getInitialDirectory())?.also {
            var initialDirectory = it
            while (!Files.exists(initialDirectory)) {
                initialDirectory = initialDirectory.parent
                gameChooser.setInitialDirectory(initialDirectory)
            }
        }
        return (gameChooser.showDialog())
    }

    private fun createStorage(): Storage {
        val newStorage = Storage()
        run {
            val nullBinding = Bindings.isNull(newStorage.pathProperty() as ObservableObjectValue<*>)
            btnReload.disableProperty().bind(nullBinding)
            btnSave.disableProperty().bind(nullBinding)
        }
        run {
            val nullBinding = Bindings.isNull(newStorage.gamePathProperty() as ObservableObjectValue<*>)
            btnReinstall.disableProperty().bind(nullBinding)
        }
        return newStorage
    }

    private fun createStorageChooser(storagePath: Path?, storageDirectory: Path?): FileChooser {
        val fileChooser = FileChooser()
        val filter = FileChooser.ExtensionFilter(
            resources.getString(LC_EXTENSION_DESCRIPTION), "*.$EXTENSION"
        )
        fileChooser.extensionFilters.add(filter)
        fileChooser.selectedExtensionFilter = filter
        if (storagePath != null) {
            fileChooser.initialDirectory = storagePath.parent.toFile()
            fileChooser.initialFileName = storagePath.fileName.toString()
        } else {
            storageDirectory?.let { fileChooser.initialDirectory = it.toFile() }
        }
        return fileChooser
    }

    private fun doClear() {
        builder.clearAll()
        updateScript(true)
    }

    private fun doInstall(gamePath: Path) {
        val context = FxContext.current
        if (!isGameDirectory(gamePath)) {
            context.error(resources.getString(LC_ERROR_NOT_GAME))
            return
        }
        try {
            Files.writeString(gamePath.resolve("game").resolve(INSTALL_NAME), generateCodeString())
            context.alert(resources.getString(LC_SUCCESS_INSTALL))
            storeGamePath(gamePath)
        } catch (e: IOException) {
            context.error(
                resources.getString(LC_ERROR_WRITE), e, mapOf(
                    ButtonData.OK_DONE to resources.getString(LC_CLOSE),
                    ButtonData.OTHER to resources.getString(LC_REPORT)
                )
            )?.let { ButtonData.OTHER == it.buttonData }.let { mailError(e) }
        } catch (e: CodeGenerationException) {
            context.error(
                resources.getString(LC_ERROR_WRITE), e, mapOf(
                    ButtonData.OK_DONE to resources.getString(LC_CLOSE),
                    ButtonData.OTHER to resources.getString(LC_REPORT)
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

    private fun doSave(): Boolean {
        val storagePath = requireNotNull(storage.getPath()) { "" }
        return if (Files.exists(storagePath) || FxContext.current.confirm(
                resources.getString(LC_SAVE_CONFIRM), mapOf(
                    ButtonData.OK_DONE to resources.getString(LC_SAVE_CONFIRM_OK),
                    ButtonData.CANCEL_CLOSE to resources.getString(LC_SAVE_CONFIRM_CANCEL)
                )
            )
        ) {
            save(storagePath)
        } else false
    }

    private fun doSaveAs(): Boolean {
        val saveFile = storageChooser.showSaveDialog(component.stage) ?: return false
        val path = saveFile.toPath()
        if (!Files.exists(path) || FxContext.current.confirm(
                resources.getString(LC_SAVE_AS_CONFIRM), mapOf(
                    ButtonData.OK_DONE to resources.getString(LC_SAVE_AS_CONFIRM_OK),
                    ButtonData.CANCEL_CLOSE to resources.getString(LC_SAVE_AS_CONFIRM_CANCEL)
                )
            )
        ) {
            save(path)
            tabSettings.storageDirectory = path.parent
        }
        return true
    }

    private fun doSaveOnClose(resources: ResourceBundle): FxCloseAction {
        val context = FxContext.current
        return if (!storage.getModified() || builder.isTreeEmpty || context.confirm(
                resources.getString(LC_CLOSE_CONFIRM), mapOf(
                    ButtonData.OK_DONE to resources.getString(LC_CLOSE_CONFIRM_OK),
                    ButtonData.CANCEL_CLOSE to resources.getString(LC_CLOSE_CONFIRM_CANCEL)
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
                resources.getString(LC_ERROR_MALFORMED_SCRIPT), e, mapOf(
                    ButtonData.OK_DONE to resources.getString(LC_CLOSE),
                    ButtonData.OTHER to resources.getString(LC_REPORT)
                )
            )?.let { ButtonData.OTHER == it.buttonData }.let { mailError(e) }
            FxCloseAction.KEEP
        } catch (e: IllegalStateException) {
            context.error(
                resources.getString(LC_ERROR_MALFORMED_SCRIPT), e, mapOf(
                    ButtonData.OK_DONE to resources.getString(LC_CLOSE),
                    ButtonData.OTHER to resources.getString(LC_REPORT)
                )
            )?.let { ButtonData.OTHER == it.buttonData }.let { mailError(e) }
            FxCloseAction.KEEP
        }
    }

    private fun generateCode(): List<String> {
        return codeConverter!!.toCode(builder.model)
    }

    private fun generateCodeString(): String {
        return generateCode().joinToString(System.lineSeparator())
    }

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
                    FxContext.current.error(
                        """
    ${resources.getString(LC_ERROR_MALFORMED_SCRIPT)}
    ${e.localizedMessage}
    """.trimIndent()
                    )
                } catch (e: IllegalStateException) {
                    creator.decorateError(setOf(e.localizedMessage))
                    FxContext.current.error(
                        """
    ${resources.getString(LC_ERROR_MALFORMED_SCRIPT)}
    ${e.localizedMessage}
    """.trimIndent()
                    )
                } catch (e: MalformedScriptException) {
                    creator.decorateError(setOf(e.localizedMessage))
                    FxContext.current.error(
                        """
    ${resources.getString(LC_ERROR_MALFORMED_SCRIPT)}
    ${e.localizedMessage}
    """.trimIndent()
                    )
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
                } catch (e: IllegalArgumentException) {
                    creator.decorateError(setOf(e.localizedMessage))
                } catch (e: IllegalStateException) {
                    creator.decorateError(setOf(e.localizedMessage))
                } catch (e: MalformedScriptException) {
                    creator.decorateError(setOf(e.localizedMessage))
                }
            }
            creator.setChanged(false)
        }
    }

    private fun save(storagePath: Path): Boolean {
        return try {
            storage.saveAs(storagePath, builder.model)
            storage.setPath(storagePath)
            true
        } catch (e: AppException) {
            FxContext.current.error(
                resources.getString(LC_ERROR_SAVE), e, mapOf(
                    ButtonData.OK_DONE to resources.getString(LC_CLOSE),
                    ButtonData.OTHER to resources.getString(LC_REPORT)
                )
            )?.let { ButtonData.OTHER == it.buttonData }.let { mailError(e) }
            false
        }
    }

    private fun storeGamePath(gamePath: Path) {
        tabSettings.gameDirectory = (gamePath)
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
        private fun createGameChooser(resources: ResourceBundle, gameDirectory: Path?): FolderChooser {
            val directoryChooser = FolderChooser()
            directoryChooser.title = resources.getString(LC_GAME_CHOOSER_TITLE)
            directoryChooser.setInitialDirectory(gameDirectory)
            directoryChooser.setSelectionFilter { isGameDirectory(it) }
            directoryChooser.setAdditionalIconProvider { path: Path?, expanded: Boolean ->
                val icon: Icon = if (isGameDirectory(path)) {
                    (if (expanded) {
                        Icon.GAME_FOLDER_OPEN
                    } else {
                        Icon.GAME_FOLDER
                    })
                } else {
                    Icon.EMPTY
                }
                ImageView(icon.findIcon(FxContext.current)!!.toExternalForm())

            }
            return directoryChooser
        }

        private fun putClipboard(content: String) {
            val clipboard = Clipboard.getSystemClipboard()
            val clipboardContent = ClipboardContent()
            clipboardContent.putString(content)
            clipboard.setContent(clipboardContent)
        }
    }
}
