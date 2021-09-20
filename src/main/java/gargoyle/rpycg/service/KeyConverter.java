package gargoyle.rpycg.service;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class KeyConverter {
    private static final KeyCombination.Modifier[] MODIFIERS = new KeyCombination.Modifier[0];
    private static final char PLUS = '+';
    private static final String UNDERSCORE = "_";

    public String toBinding(final KeyCodeCombination key) {
        final StringBuilder sb = new StringBuilder(32);
        boolean complex = false;
        if (KeyCombination.ModifierValue.DOWN == key.getShift()) {
            sb.append(KeyCode.SHIFT.getName().toLowerCase(Locale.ENGLISH));
            complex = true;
        }
        if (KeyCombination.ModifierValue.DOWN == key.getControl()) {
            if (complex) {
                sb.append(UNDERSCORE);
            }
            sb.append(KeyCode.CONTROL.getName().toLowerCase(Locale.ENGLISH));
            complex = true;
        }
        if (KeyCombination.ModifierValue.DOWN == key.getAlt()) {
            if (complex) {
                sb.append(UNDERSCORE);
            }
            sb.append(KeyCode.ALT.getName().toLowerCase(Locale.ENGLISH));
            complex = true;
        }
        if (KeyCombination.ModifierValue.DOWN == key.getMeta()) {
            if (complex) {
                sb.append(UNDERSCORE);
            }
            sb.append(KeyCode.META.getName().toLowerCase(Locale.ENGLISH));
            complex = true;
        }
        if (complex) {
            sb.append(UNDERSCORE);
        }
        sb.append(key.getCode().getName().toUpperCase(Locale.ENGLISH));
        return sb.toString();
    }

    public Optional<KeyCodeCombination> toCombination(final String keys) {
        try {
            KeyCode keyCode = KeyCode.UNDEFINED;
            final Set<KeyCombination.Modifier> modifiers = new HashSet<>(4);
            for (final String name : keys.split(UNDERSCORE)) {
                final KeyCode value = KeyCode.valueOf(name);
                switch (value) {
                    case SHIFT:
                        modifiers.add(KeyCombination.SHIFT_DOWN);
                        break;
                    case CONTROL:
                        modifiers.add(KeyCombination.CONTROL_DOWN);
                        break;
                    case ALT:
                        modifiers.add(KeyCombination.ALT_DOWN);
                        break;
                    case META:
                        modifiers.add(KeyCombination.META_DOWN);
                        break;
                    default:
                        keyCode = value;
                }
            }
            return Optional.of(new KeyCodeCombination(keyCode, modifiers.toArray(MODIFIERS)));
        } catch (final IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public String toString(final KeyCodeCombination keyCodeCombination) {
        final StringBuilder sb = new StringBuilder(32);
        boolean complex = false;
        if (KeyCombination.ModifierValue.DOWN == keyCodeCombination.getShift()) {
            sb.append(KeyCode.SHIFT.getName().toUpperCase(Locale.ENGLISH));
            complex = true;
        }
        if (KeyCombination.ModifierValue.DOWN == keyCodeCombination.getControl()) {
            if (complex) {
                sb.append(PLUS);
            }
            sb.append(KeyCode.CONTROL.getName().toUpperCase(Locale.ENGLISH));
            complex = true;
        }
        if (KeyCombination.ModifierValue.DOWN == keyCodeCombination.getAlt()) {
            if (complex) {
                sb.append(PLUS);
            }
            sb.append(KeyCode.ALT.getName().toUpperCase(Locale.ENGLISH));
            complex = true;
        }
        if (KeyCombination.ModifierValue.DOWN == keyCodeCombination.getMeta()) {
            if (complex) {
                sb.append(PLUS);
            }
            sb.append(KeyCode.META.getName().toUpperCase(Locale.ENGLISH));
            complex = true;
        }
        if (complex) {
            sb.append(PLUS);
        }
        sb.append(keyCodeCombination.getCode().getName().toUpperCase(Locale.ENGLISH));
        return sb.toString();
    }
}
