package gargoyle.rpycg.ui

import gargoyle.fx.FxContext
import gargoyle.fx.FxLauncher.requestRestart
import gargoyle.fx.FxUtil.get
import gargoyle.rpycg.model.Settings
import gargoyle.rpycg.service.LocaleConverter
import gargoyle.rpycg.ui.flags.Flags
import javafx.beans.binding.Bindings
import javafx.beans.value.ObservableValue
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.MenuButton
import javafx.scene.control.MenuItem
import javafx.scene.image.ImageView
import javafx.scene.layout.GridPane
import javafx.util.Callback
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Locale
import java.util.ResourceBundle
import java.util.prefs.Preferences

class TabSettings : GridPane(), Initializable {
    private val localeConverter: LocaleConverter
    private val preferences: Preferences

    @FXML
    private lateinit var chkEnableCheat: CheckBox

    @FXML
    private lateinit var chkEnableConsole: CheckBox

    @FXML
    private lateinit var chkEnableDeveloper: CheckBox

    @FXML
    private lateinit var chkEnableRollback: CheckBox

    @FXML
    private lateinit var chkEnableWrite: CheckBox

    @FXML
    private lateinit var cmbLocaleMenu: ComboBox<Locale>

    @FXML
    private lateinit var cmbLocaleUi: MenuButton

    @FXML
    private lateinit var keyCheat: KeyText

    @FXML
    private lateinit var keyConsole: KeyText

    @FXML
    private lateinit var keyDeveloper: KeyText

    @FXML
    private lateinit var keyWrite: KeyText
    lateinit var settings: Settings
        private set

    init {
        val context = FxContext.current
        preferences = context.preferences
        localeConverter = LocaleConverter(context)
        context.loadComponent(this) ?: error("No view {TabSettings}")
    }

    var gameDirectory: Path
        get() = Paths.get(preferences[PREF_GAME, USER_HOME])
        set(gameDirectory) = preferences.put(PREF_GAME, gameDirectory.toFile().absolutePath)
    var storageDirectory: Path
        get() = Paths.get(preferences[PREF_STORAGE, USER_HOME])
        set(storageDirectory) = preferences.put(PREF_STORAGE, storageDirectory.toFile().absolutePath)

    override fun initialize(location: URL, resources: ResourceBundle) {
        settings = Settings(
            localeConverter,
            preferences,
            chkEnableCheat.isSelected,
            chkEnableConsole.isSelected,
            chkEnableDeveloper.isSelected,
            chkEnableWrite.isSelected,
            chkEnableRollback.isSelected,
            keyCheat.getDefaultCombination(),
            keyConsole.getDefaultCombination(),
            keyDeveloper.getDefaultCombination(),
            keyWrite.getDefaultCombination(),
            Locale.ENGLISH
        ).also {
            with(it) {
                this@TabSettings.cmbLocaleMenu.selectionModel.select(getLocaleMenu())
                this@TabSettings.chkEnableRollback.selectedProperty().value = enableRollbackProperty().value
                this@TabSettings.chkEnableCheat.selectedProperty().value = enableCheatProperty().value
                this@TabSettings.chkEnableConsole.selectedProperty().value = enableConsoleProperty().value
                this@TabSettings.chkEnableDeveloper.selectedProperty().value = enableDeveloperProperty().value
                this@TabSettings.chkEnableWrite.selectedProperty().value = enableWriteProperty().value
                this@TabSettings.keyCheat.defaultCombinationProperty().value = keyCheatProperty().value
                this@TabSettings.keyConsole.defaultCombinationProperty().value = keyConsoleProperty().value
                this@TabSettings.keyDeveloper.defaultCombinationProperty().value = keyDeveloperProperty().value
                this@TabSettings.keyWrite.defaultCombinationProperty().value = keyWriteProperty().value
                localeMenuProperty().bind(this@TabSettings.cmbLocaleMenu.selectionModel.selectedItemProperty())
                enableRollbackProperty().bind(this@TabSettings.chkEnableRollback.selectedProperty())
                enableCheatProperty().bind(this@TabSettings.chkEnableCheat.selectedProperty())
                enableConsoleProperty().bind(this@TabSettings.chkEnableConsole.selectedProperty())
                enableDeveloperProperty().bind(this@TabSettings.chkEnableDeveloper.selectedProperty())
                enableWriteProperty().bind(this@TabSettings.chkEnableWrite.selectedProperty())
                keyCheatProperty().bind(this@TabSettings.keyCheat.combinationProperty())
                keyConsoleProperty().bind(this@TabSettings.keyConsole.combinationProperty())
                keyDeveloperProperty().bind(this@TabSettings.keyDeveloper.combinationProperty())
                keyWriteProperty().bind(this@TabSettings.keyWrite.combinationProperty())
                this@TabSettings.keyCheat.disableProperty()
                    .bind(Bindings.not(this@TabSettings.chkEnableCheat.selectedProperty()))
                this@TabSettings.keyConsole.disableProperty()
                    .bind(Bindings.not(this@TabSettings.chkEnableConsole.selectedProperty()))
                this@TabSettings.keyDeveloper.disableProperty()
                    .bind(Bindings.not(this@TabSettings.chkEnableDeveloper.selectedProperty()))
                this@TabSettings.keyWrite.disableProperty()
                    .bind(Bindings.not(this@TabSettings.chkEnableWrite.selectedProperty()))
            }
        }
        initializeLocaleComboBox(cmbLocaleMenu, settings.getLocaleMenu())
        initializeLocaleUi(resources)
    }

    @Suppress("UNUSED_PARAMETER")
    @FXML
    fun onResetKeys(actionEvent: ActionEvent): Unit =
        arrayOf(keyCheat, keyConsole, keyDeveloper, keyWrite).forEach(KeyText::reset)

    private fun initializeLocaleComboBox(comboBox: ComboBox<Locale>, locale: Locale) {
        with(comboBox) {
            val factory = Callback<ListView<Locale>, ListCell<Locale>> {
                DecoratedListCell { cell: ListCell<Locale>, item: Locale? ->
                    with(cell) {
                        if (item == null) {
                            graphic = null
                            setText("")
                        } else {
                            Flags.getFlag(FxContext.current, item.language)?.let { setGraphic(it) }
                            setText(localeConverter.toDisplayString(item))
                        }
                    }
                }
            }
            buttonCell = factory.call(null)
            cellFactory = factory
            items.setAll(localeConverter.locales)
            localeConverter.getSimilarLocale(locale).let {
                value = it
                selectionModel.select(it)
            }
        }
    }

    private fun initializeLocaleUi(resources: ResourceBundle) {
        val context = FxContext.current
        cmbLocaleUi.items.setAll(localeConverter.locales.mapNotNull { locale: Locale ->
            Flags.getFlag(context, locale.language)
                ?.let { imageView: ImageView? ->
                    val menuItem = MenuItem(localeConverter.toDisplayString(locale), imageView)
                    menuItem.onAction = EventHandler {
                        FxContext.current =
                            context.toBuilder().setLocale(localeConverter.getSimilarLocale(locale)).build()
                        if (context.confirm(
                                resources[LC_NEED_RESTART], mapOf(
                                    ButtonData.OK_DONE to resources[LC_NEED_RESTART_OK],
                                    ButtonData.CANCEL_CLOSE to resources[LC_NEED_RESTART_CANCEL]
                                )
                            )
                        ) requestRestart(context.toBuilder().setLocale(locale).build())
                    }
                    menuItem
                }
        })
        settings.localeMenuProperty().addListener { _: ObservableValue<out Locale>, _: Locale?, newValue: Locale ->
            cmbLocaleUi.text = localeConverter.toDisplayString(newValue)
            Flags.getFlag(context, newValue.language)?.let { cmbLocaleUi.graphic = it }
        }
        val currentLocale = localeConverter.getSimilarLocale(context.locale)
        cmbLocaleUi.text = localeConverter.toDisplayString(currentLocale)
        Flags.getFlag(context, currentLocale.language)?.let { cmbLocaleUi.graphic = it }
    }

    private class DecoratedListCell<T : Any>(private val decorator: (ListCell<T>, T) -> Unit) : ListCell<T>() {

        override fun updateItem(item: T?, empty: Boolean) {
            super.updateItem(item, empty)
            if (!empty && item != null) decorator(this, item)
        }
    }

    companion object {
        private const val LC_NEED_RESTART = "need-restart"
        private const val LC_NEED_RESTART_CANCEL = "need-restart-cancel"
        private const val LC_NEED_RESTART_OK = "need-restart-ok"
        private const val PREF_GAME = "game"
        private const val PREF_STORAGE = "storage"
        private val USER_HOME = System.getProperty("user.home")
    }
}
