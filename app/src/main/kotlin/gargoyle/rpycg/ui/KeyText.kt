package gargoyle.rpycg.ui

import gargoyle.fx.FxContext
import gargoyle.fx.FxRun
import gargoyle.fx.FxUtil.get
import javafx.beans.DefaultProperty
import javafx.beans.property.Property
import javafx.beans.property.ReadOnlyProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.scene.control.TextField
import javafx.scene.control.Tooltip
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.input.KeyEvent
import java.util.*

@DefaultProperty("keyCode")
class KeyText : TextField() {
    private val activated: Property<Boolean> = SimpleBooleanProperty(false)
    private val combination: Property<KeyCodeCombination> = SimpleObjectProperty(NONE)
    private val defaultCombination: Property<KeyCodeCombination> = SimpleObjectProperty(NONE)

    init {
        val resources: ResourceBundle = FxContext.current.loadResources(KeyText::class)!!
        val tooltip = resources[LC_DEFAULT, LC_DEFAULT]
        val pressKey = resources[LC_PRESS_ANY_KEY, LC_PRESS_ANY_KEY]
        defaultCombination.addListener { _: ObservableValue<out KeyCodeCombination>, oldValue: KeyCodeCombination?, newValue: KeyCodeCombination ->
            if (null == oldValue || NONE == oldValue) combination.value = newValue
            updateTooltip(newValue, tooltip)
        }
        updateTooltip(defaultCombination.value, tooltip)
        combination.addListener { _: ObservableValue<out KeyCodeCombination>, oldValue: KeyCodeCombination?, newValue: KeyCodeCombination ->
            if (null == oldValue) {
                updateTooltip(newValue, tooltip)
            }
            text = newValue.displayText
        }
        activated.addListener { _: ObservableValue<out Boolean>, _: Boolean, newValue: Boolean ->
            text = if (newValue) pressKey else combination.value!!.displayText
        }
        isEditable = false
        onKeyReleased = EventHandler {
            if (activated.value) {
                if (it.code.isLetterKey || it.code.isDigitKey) combination.value = toCombination(it)
                activated.value = false
            }
        }
        focusedProperty().addListener { _: ObservableValue<out Boolean>, _: Boolean, newValue: Boolean ->
            activated.setValue(newValue)
        }
        onMouseClicked = EventHandler {
            requestFocus()
            activated.setValue(true)
        }
    }

    fun activatedProperty(): ReadOnlyProperty<Boolean> {
        return activated
    }

    fun combinationProperty(): ReadOnlyProperty<KeyCodeCombination> {
        return combination
    }

    fun defaultCombinationProperty(): Property<KeyCodeCombination> {
        return defaultCombination
    }

    fun getActivated(): Boolean {
        return activated.value
    }

    fun getCombination(): KeyCodeCombination {
        return combination.value
    }

    fun getDefaultCombination(): KeyCodeCombination {
        return defaultCombination.value
    }

    fun setDefaultCombination(defaultCombination: KeyCodeCombination) {
        this.defaultCombination.value = defaultCombination
    }

    fun reset() {
        combination.value = defaultCombination.value
        activated.value = false
    }

    private fun updateTooltip(keyCodeCombination: KeyCodeCombination, tooltipString: String) {
        FxRun.runLater {
            tooltip = Tooltip("$tooltipString ${keyCodeCombination.displayText}")
        }
    }

    companion object {
        val NONE: KeyCodeCombination = KeyCodeCombination(KeyCode.CODE_INPUT)
        private const val LC_DEFAULT = "default-is"
        private const val LC_PRESS_ANY_KEY = "press-any-key"
        private fun toCombination(event: KeyEvent): KeyCodeCombination =
            KeyCodeCombination(
                event.code,
                if (event.isShiftDown) KeyCombination.ModifierValue.DOWN else KeyCombination.ModifierValue.UP,
                if (event.isControlDown) KeyCombination.ModifierValue.DOWN else KeyCombination.ModifierValue.UP,
                if (event.isAltDown) KeyCombination.ModifierValue.DOWN else KeyCombination.ModifierValue.UP,
                if (event.isMetaDown) KeyCombination.ModifierValue.DOWN else KeyCombination.ModifierValue.UP,
                if (event.isShortcutDown) KeyCombination.ModifierValue.DOWN else KeyCombination.ModifierValue.UP
            )
    }
}
