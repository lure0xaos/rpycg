package gargoyle.rpycg.service

import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination

class KeyConverter {
    fun toBinding(key: KeyCodeCombination): String =
        with(mutableListOf<String>()) {
            if (key.shift == KeyCombination.ModifierValue.DOWN) this += KeyCode.SHIFT.getName().lowercase()
            if (key.control == KeyCombination.ModifierValue.DOWN) this += KeyCode.CONTROL.getName().lowercase()
            if (key.alt == KeyCombination.ModifierValue.DOWN) this += KeyCode.ALT.getName().lowercase()
            if (key.meta == KeyCombination.ModifierValue.DOWN) this += KeyCode.META.getName().lowercase()
            this += key.code.getName().uppercase()
            this
        }.joinToString(UNDERSCORE)

    fun toCombination(keys: String): KeyCodeCombination? =
        try {
            var keyCode: KeyCode = KeyCode.UNDEFINED
            val modifiers: MutableList<KeyCombination.Modifier> = mutableListOf()
            for (name in keys.split(UNDERSCORE)) {
                when (val value = KeyCode.valueOf(name)) {
                    KeyCode.SHIFT -> modifiers += KeyCombination.SHIFT_DOWN
                    KeyCode.CONTROL -> modifiers += KeyCombination.CONTROL_DOWN
                    KeyCode.ALT -> modifiers += KeyCombination.ALT_DOWN
                    KeyCode.META -> modifiers += KeyCombination.META_DOWN
                    else -> keyCode = value
                }
            }
            KeyCodeCombination(keyCode, *modifiers.toTypedArray())
        } catch (e: IllegalArgumentException) {
            null
        }

    fun toString(key: KeyCodeCombination): String =
        with(mutableListOf<String>()) {
            if (key.shift == KeyCombination.ModifierValue.DOWN) this += (KeyCode.SHIFT.getName().uppercase())
            if (key.control == KeyCombination.ModifierValue.DOWN) this += (KeyCode.CONTROL.getName().uppercase())
            if (key.alt == KeyCombination.ModifierValue.DOWN) this += (KeyCode.ALT.getName().uppercase())
            if (key.meta == KeyCombination.ModifierValue.DOWN) this += (KeyCode.META.getName().uppercase())
            this += (key.code.getName().uppercase())
            this
        }.joinToString(PLUS)

    companion object {
        private const val PLUS = "+"
        private const val UNDERSCORE = "_"
    }
}
