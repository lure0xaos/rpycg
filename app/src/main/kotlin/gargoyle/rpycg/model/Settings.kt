package gargoyle.rpycg.model

import gargoyle.rpycg.service.KeyConverter
import gargoyle.rpycg.service.LocaleConverter
import gargoyle.rpycg.ui.KeyText
import javafx.beans.property.Property
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.scene.input.KeyCodeCombination
import java.util.Locale
import java.util.prefs.Preferences

class Settings(
    enableCheat: Boolean, enableConsole: Boolean, enableDeveloper: Boolean,
    enableWrite: Boolean, enableRollback: Boolean,
    keyCheat: KeyCodeCombination, keyConsole: KeyCodeCombination,
    keyDeveloper: KeyCodeCombination, keyWrite: KeyCodeCombination,
    localeMenu: Locale
) {
    private val converter: KeyConverter = KeyConverter()
    private val enableCheat: Property<Boolean>
    private val enableConsole: Property<Boolean>
    private val enableDeveloper: Property<Boolean>
    private val enableRollback: Property<Boolean>
    private val enableWrite: Property<Boolean>
    private val keyCheat: Property<KeyCodeCombination>
    private val keyConsole: Property<KeyCodeCombination>
    private val keyDeveloper: Property<KeyCodeCombination>
    private val keyWrite: Property<KeyCodeCombination>
    private val localeMenu: Property<Locale>

    constructor(
        localeConverter: LocaleConverter,
        preferences: Preferences,
        enableCheat: Boolean,
        enableConsole: Boolean,
        enableDeveloper: Boolean,
        enableWrite: Boolean,
        enableRollback: Boolean,
        keyCheat: KeyCodeCombination,
        keyConsole: KeyCodeCombination,
        keyDeveloper: KeyCodeCombination,
        keyWrite: KeyCodeCombination,
        localeMenu: Locale
    ) : this(
        enableCheat,
        enableConsole,
        enableDeveloper,
        enableWrite,
        enableRollback,
        keyCheat,
        keyConsole,
        keyDeveloper,
        keyWrite,
        localeMenu
    ) {
        this.localeMenu.value =
            localeConverter.toLocale(preferences[PREF_LOCALE_MENU, localeConverter.toString(localeMenu)])
        this.enableCheat.value = preferences.getBoolean(PREF_CHEAT, enableCheat)
        this.enableConsole.value = preferences.getBoolean(PREF_CONSOLE, enableConsole)
        this.enableDeveloper.value = preferences.getBoolean(PREF_DEVELOPER, enableDeveloper)
        this.enableWrite.value = preferences.getBoolean(PREF_WRITE, enableWrite)
        this.keyCheat.value =
            converter.toCombination(preferences[PREF_KEY_CHEAT, converter.toString(keyCheat)]) ?: keyCheat
        this.keyConsole.value =
            converter.toCombination(preferences[PREF_KEY_CONSOLE, converter.toString(keyConsole)]) ?: keyConsole
        this.keyDeveloper.value =
            converter.toCombination(preferences[PREF_KEY_DEVELOPER, converter.toString(keyDeveloper)])
                ?: keyDeveloper
        this.keyWrite.value =
            converter.toCombination(preferences[PREF_KEY_WRITE, converter.toString(keyWrite)]) ?: keyWrite
        this.localeMenu.addListener { _: ObservableValue<out Locale>, _: Locale, newValue: Locale ->
            preferences.put(PREF_LOCALE_MENU, localeConverter.toString(newValue))
        }
        this.enableCheat.addListener { _: ObservableValue<out Boolean>, _: Boolean, newValue: Boolean ->
            preferences.putBoolean(PREF_CHEAT, newValue)
        }
        this.enableConsole.addListener { _: ObservableValue<out Boolean>, _: Boolean, newValue: Boolean ->
            preferences.putBoolean(PREF_CONSOLE, newValue)
        }
        this.enableDeveloper.addListener { _: ObservableValue<out Boolean>, _: Boolean, newValue: Boolean ->
            preferences.putBoolean(PREF_DEVELOPER, newValue)
        }
        this.enableWrite.addListener { _: ObservableValue<out Boolean>, _: Boolean, newValue: Boolean ->
            preferences.putBoolean(PREF_WRITE, newValue)
        }
        this.enableRollback.addListener { _: ObservableValue<out Boolean>, _: Boolean, newValue: Boolean ->
            preferences.putBoolean(PREF_ROLLBACK, newValue)
        }
        this.keyCheat.addListener { _: ObservableValue<out KeyCodeCombination>, _: KeyCodeCombination, newValue: KeyCodeCombination ->
            if (newValue != KeyText.NONE)
                preferences.put(PREF_KEY_CHEAT, converter.toString(newValue))
        }
        this.keyConsole.addListener { _: ObservableValue<out KeyCodeCombination>, _: KeyCodeCombination, newValue: KeyCodeCombination ->
            if (newValue != KeyText.NONE)
                preferences.put(PREF_KEY_CONSOLE, converter.toString(newValue))
        }
        this.keyDeveloper.addListener { _: ObservableValue<out KeyCodeCombination>, _: KeyCodeCombination, newValue: KeyCodeCombination ->
            if (newValue != KeyText.NONE)
                preferences.put(PREF_KEY_DEVELOPER, converter.toString(newValue))
        }
        this.keyWrite.addListener { _: ObservableValue<out KeyCodeCombination>, _: KeyCodeCombination, newValue: KeyCodeCombination ->
            if (newValue != KeyText.NONE)
                preferences.put(PREF_KEY_WRITE, converter.toString(newValue))
        }
    }

    init {
        this.enableCheat = SimpleBooleanProperty(enableCheat)
        this.enableConsole = SimpleBooleanProperty(enableConsole)
        this.enableDeveloper = SimpleBooleanProperty(enableDeveloper)
        this.enableWrite = SimpleBooleanProperty(enableWrite)
        this.enableRollback = SimpleBooleanProperty(enableRollback)
        this.keyCheat = SimpleObjectProperty(keyCheat)
        this.keyConsole = SimpleObjectProperty(keyConsole)
        this.keyDeveloper = SimpleObjectProperty(keyDeveloper)
        this.keyWrite = SimpleObjectProperty(keyWrite)
        this.localeMenu = SimpleObjectProperty(localeMenu)
    }

    fun enableCheatProperty(): Property<Boolean> =
        enableCheat

    fun enableConsoleProperty(): Property<Boolean> =
        enableConsole

    fun enableDeveloperProperty(): Property<Boolean> =
        enableDeveloper

    fun enableRollbackProperty(): Property<Boolean> =
        enableRollback

    fun enableWriteProperty(): Property<Boolean> =
        enableWrite

    fun getEnableCheat(): Boolean =
        enableCheat.value

    fun setEnableCheat(enableCheat: Boolean) {
        this.enableCheat.value = enableCheat
    }

    fun getEnableConsole(): Boolean = enableConsole.value

    fun setEnableConsole(enableConsole: Boolean) {
        this.enableConsole.value = enableConsole
    }

    fun getEnableDeveloper(): Boolean = enableDeveloper.value

    fun setEnableDeveloper(enableDeveloper: Boolean) {
        this.enableDeveloper.value = enableDeveloper
    }

    fun getEnableRollback(): Boolean = enableRollback.value

    fun setEnableRollback(enableRollback: Boolean) {
        this.enableRollback.value = enableRollback
    }

    fun getEnableWrite(): Boolean = enableWrite.value

    fun setEnableWrite(enableWrite: Boolean) {
        this.enableWrite.value = enableWrite
    }

    fun getKeyCheat(): KeyCodeCombination =
        keyCheat.value

    fun setKeyCheat(keyCheat: KeyCodeCombination) {
        this.keyCheat.value = keyCheat
    }

    fun getKeyConsole(): KeyCodeCombination =
        keyConsole.value

    fun setKeyConsole(keyConsole: KeyCodeCombination) {
        this.keyConsole.value = keyConsole
    }

    fun getKeyDeveloper(): KeyCodeCombination =
        keyDeveloper.value

    fun setKeyDeveloper(keyDeveloper: KeyCodeCombination) {
        this.keyDeveloper.value = keyDeveloper
    }

    fun getKeyWrite(): KeyCodeCombination =
        keyWrite.value

    fun setKeyWrite(keyWrite: KeyCodeCombination) {
        this.keyWrite.value = keyWrite
    }

    fun getLocaleMenu(): Locale =
        localeMenu.value

    fun setLocaleMenu(locale: Locale) {
        localeMenu.value = locale
    }

    fun keyCheatProperty(): Property<KeyCodeCombination> =
        keyCheat

    fun keyConsoleProperty(): Property<KeyCodeCombination> =
        keyConsole

    fun keyDeveloperProperty(): Property<KeyCodeCombination> =
        keyDeveloper

    fun keyWriteProperty(): Property<KeyCodeCombination> =
        keyWrite

    fun localeMenuProperty(): Property<Locale> =
        localeMenu

    companion object {
        const val PREF_KEY_CHEAT: String = "keyCheat"
        const val PREF_KEY_CONSOLE: String = "keyConsole"
        const val PREF_KEY_DEVELOPER: String = "keyDeveloper"
        const val PREF_KEY_WRITE: String = "keyWrite"
        private const val PREF_CHEAT = "cheat"
        private const val PREF_CONSOLE = "console"
        private const val PREF_DEVELOPER = "developer"
        private const val PREF_LOCALE_MENU = "localeMenu"
        private const val PREF_ROLLBACK = "rollback"
        private const val PREF_WRITE = "write"
    }
}
