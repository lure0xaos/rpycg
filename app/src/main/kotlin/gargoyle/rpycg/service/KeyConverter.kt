package gargoyle.rpycg.service

import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination

class KeyConverter {
    fun toBinding(key: KeyCodeCombination): String {
        val sb = StringBuilder(32)
        var complex = false
        if (KeyCombination.ModifierValue.DOWN == key.shift) {
            sb.append(KeyCode.SHIFT.getName().lowercase())
            complex = true
        }
        if (KeyCombination.ModifierValue.DOWN == key.control) {
            if (complex) sb.append(UNDERSCORE)
            sb.append(KeyCode.CONTROL.getName().lowercase())
            complex = true
        }
        if (KeyCombination.ModifierValue.DOWN == key.alt) {
            if (complex) sb.append(UNDERSCORE)
            sb.append(KeyCode.ALT.getName().lowercase())
            complex = true
        }
        if (KeyCombination.ModifierValue.DOWN == key.meta) {
            if (complex) sb.append(UNDERSCORE)
            sb.append(KeyCode.META.getName().lowercase())
            complex = true
        }
        if (complex) sb.append(UNDERSCORE)
        sb.append(key.code.getName().uppercase())
        return sb.toString()
    }

    fun toCombination(keys: String): KeyCodeCombination? =
        try {
            var keyCode: KeyCode = KeyCode.UNDEFINED
            val modifiers: MutableSet<KeyCombination.Modifier> = HashSet(4)
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

    fun toString(keyCodeCombination: KeyCodeCombination): String {
        val sb = StringBuilder(32)
        var complex = false
        if (KeyCombination.ModifierValue.DOWN == keyCodeCombination.shift) {
            sb.append(KeyCode.SHIFT.getName().uppercase())
            complex = true
        }
        if (KeyCombination.ModifierValue.DOWN == keyCodeCombination.control) {
            if (complex) sb.append(PLUS)
            sb.append(KeyCode.CONTROL.getName().uppercase())
            complex = true
        }
        if (KeyCombination.ModifierValue.DOWN == keyCodeCombination.alt) {
            if (complex) sb.append(PLUS)
            sb.append(KeyCode.ALT.getName().uppercase())
            complex = true
        }
        if (KeyCombination.ModifierValue.DOWN == keyCodeCombination.meta) {
            if (complex) sb.append(PLUS)
            sb.append(KeyCode.META.getName().uppercase())
            complex = true
        }
        if (complex) sb.append(PLUS)
        sb.append(keyCodeCombination.code.getName().uppercase())
        return sb.toString()
    }

    companion object {
        private const val PLUS = '+'
        private const val UNDERSCORE = "_"
    }
}
