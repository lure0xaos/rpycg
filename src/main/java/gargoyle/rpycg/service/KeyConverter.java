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

    public String toBinding(KeyCodeCombination key) {
        StringBuilder sb = new StringBuilder(32);
        boolean complex = false;
        if (key.getShift() == KeyCombination.ModifierValue.DOWN) {
            sb.append(KeyCode.SHIFT.getName().toLowerCase(Locale.ENGLISH));
            complex = true;
        }
        if (key.getControl() == KeyCombination.ModifierValue.DOWN) {
            if (complex) {
                sb.append(UNDERSCORE);
            }
            sb.append(KeyCode.CONTROL.getName().toLowerCase(Locale.ENGLISH));
            complex = true;
        }
        if (key.getAlt() == KeyCombination.ModifierValue.DOWN) {
            if (complex) {
                sb.append(UNDERSCORE);
            }
            sb.append(KeyCode.ALT.getName().toLowerCase(Locale.ENGLISH));
            complex = true;
        }
        if (key.getMeta() == KeyCombination.ModifierValue.DOWN) {
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

    public Optional<KeyCodeCombination> toCombination(String keys) {
        try {
            KeyCode keyCode = KeyCode.UNDEFINED;
            Set<KeyCombination.Modifier> modifiers = new HashSet<>(4);
            for (String name : keys.split(UNDERSCORE)) {
                KeyCode value = KeyCode.valueOf(name);
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
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public String toString(KeyCodeCombination keyCodeCombination) {
        StringBuilder sb = new StringBuilder(32);
        boolean complex = false;
        if (keyCodeCombination.getShift() == KeyCombination.ModifierValue.DOWN) {
            sb.append(KeyCode.SHIFT.getName().toUpperCase(Locale.ENGLISH));
            complex = true;
        }
        if (keyCodeCombination.getControl() == KeyCombination.ModifierValue.DOWN) {
            if (complex) {
                sb.append(PLUS);
            }
            sb.append(KeyCode.CONTROL.getName().toUpperCase(Locale.ENGLISH));
            complex = true;
        }
        if (keyCodeCombination.getAlt() == KeyCombination.ModifierValue.DOWN) {
            if (complex) {
                sb.append(PLUS);
            }
            sb.append(KeyCode.ALT.getName().toUpperCase(Locale.ENGLISH));
            complex = true;
        }
        if (keyCodeCombination.getMeta() == KeyCombination.ModifierValue.DOWN) {
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
