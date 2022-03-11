package gargoyle.rpycg.ui

import gargoyle.fx.FxContext
import gargoyle.fx.FxLauncher.requestRestart
import gargoyle.rpycg.ex.AppUserException
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
        context.loadComponent(this) ?: throw  AppUserException("No view {TabSettings}")
    }

    var gameDirectory: Path
        get() = Paths.get(preferences[PREF_GAME, USER_HOME])
        set(gameDirectory) = preferences.put(PREF_GAME, gameDirectory.toFile().absolutePath)
    var storageDirectory: Path
        get() = Paths.get(preferences[PREF_STORAGE, USER_HOME])
        set(storageDirectory) = preferences.put(PREF_STORAGE, storageDirectory.toFile().absolutePath)

    override fun initialize(location: URL, resources: ResourceBundle) {
        settings = initializeSettings(
            Settings(
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
            )
        )
        initializeLocaleComboBox(cmbLocaleMenu, settings.getLocaleMenu())
        initializeLocaleUi(resources)
    }

    @Suppress("UNUSED_PARAMETER")
    @FXML
    fun onResetKeys(actionEvent: ActionEvent) =
        arrayOf(keyCheat, keyConsole, keyWrite).forEach(KeyText::reset)

    private fun initializeLocaleComboBox(comboBox: ComboBox<Locale>, locale: Locale) {
        val cellFactory = Callback<ListView<Locale>, ListCell<Locale>> {
            DecoratedListCell { cell: ListCell<Locale>, item: Locale? ->
                if (null == item) {
                    cell.graphic = null
                    cell.setText("")
                } else {
                    Flags.getFlag(FxContext.current, item.language)?.let { cell.setGraphic(it) }
                    cell.setText(localeConverter.toDisplayString(item))
                }
            }
        }
        comboBox.buttonCell = cellFactory.call(null)
        comboBox.cellFactory = cellFactory
        comboBox.items.setAll(localeConverter.locales)
        val similarLocale = localeConverter.getSimilarLocale(locale)
        comboBox.value = similarLocale
        comboBox.selectionModel.select(similarLocale)
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
                                resources.getString(LC_NEED_RESTART), mapOf(
                                    ButtonData.OK_DONE to resources.getString(LC_NEED_RESTART_OK),
                                    ButtonData.CANCEL_CLOSE to resources.getString(LC_NEED_RESTART_CANCEL)
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

    private fun initializeSettings(set: Settings): Settings {
        cmbLocaleMenu.selectionModel.select(set.getLocaleMenu())
        chkEnableRollback.selectedProperty().value = set.enableRollbackProperty().value
        chkEnableCheat.selectedProperty().value = set.enableCheatProperty().value
        chkEnableConsole.selectedProperty().value = set.enableConsoleProperty().value
        chkEnableDeveloper.selectedProperty().value = set.enableDeveloperProperty().value
        chkEnableWrite.selectedProperty().value = set.enableWriteProperty().value
        keyCheat.defaultCombinationProperty().value = set.keyCheatProperty().value
        keyConsole.defaultCombinationProperty().value = set.keyConsoleProperty().value
        keyDeveloper.defaultCombinationProperty().value = set.keyDeveloperProperty().value
        keyWrite.defaultCombinationProperty().value = set.keyWriteProperty().value
        set.localeMenuProperty().bind(cmbLocaleMenu.selectionModel.selectedItemProperty())
        set.enableRollbackProperty().bind(chkEnableRollback.selectedProperty())
        set.enableCheatProperty().bind(chkEnableCheat.selectedProperty())
        set.enableConsoleProperty().bind(chkEnableConsole.selectedProperty())
        set.enableDeveloperProperty().bind(chkEnableDeveloper.selectedProperty())
        set.enableWriteProperty().bind(chkEnableWrite.selectedProperty())
        set.keyCheatProperty().bind(keyCheat.combinationProperty())
        set.keyConsoleProperty().bind(keyConsole.combinationProperty())
        set.keyDeveloperProperty().bind(keyDeveloper.combinationProperty())
        set.keyWriteProperty().bind(keyWrite.combinationProperty())
        keyCheat.disableProperty().bind(Bindings.not(chkEnableCheat.selectedProperty()))
        keyConsole.disableProperty().bind(Bindings.not(chkEnableConsole.selectedProperty()))
        keyDeveloper.disableProperty().bind(Bindings.not(chkEnableDeveloper.selectedProperty()))
        keyWrite.disableProperty().bind(Bindings.not(chkEnableWrite.selectedProperty()))
        return set
    }

    private class DecoratedListCell<T : Any>(private val decorator: (ListCell<T>, T?) -> Unit) : ListCell<T>() {

        override fun updateItem(item: T?, empty: Boolean) {
            super.updateItem(item, empty)
            if (!empty) decorator(this, item)
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
