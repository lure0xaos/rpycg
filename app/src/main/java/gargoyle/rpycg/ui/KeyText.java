package gargoyle.rpycg.ui;

import gargoyle.fx.FXContextFactory;
import javafx.beans.DefaultProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import java.util.Optional;
import java.util.ResourceBundle;

@DefaultProperty("keyCode")
public final class KeyText extends TextField {
    private static final String LC_DEFAULT = "default-is";
    private static final String LC_PRESS_ANY_KEY = "press-any-key";
    private final Property<Boolean> activated;
    private final Property<KeyCodeCombination> combination;
    private final Property<KeyCodeCombination> defaultCombination;

    public KeyText() {
        defaultCombination = new SimpleObjectProperty<>(null);
        combination = new SimpleObjectProperty<>(null);
        activated = new SimpleBooleanProperty(false);
        final Optional<ResourceBundle> optional = FXContextFactory.currentContext().loadResources(KeyText.class);
        final String tooltip = optional.map(resources -> resources.getString(LC_DEFAULT)).orElse(LC_DEFAULT);
        final String pressKey = optional.map(resources -> resources.getString(LC_PRESS_ANY_KEY)).orElse(LC_PRESS_ANY_KEY);
        defaultCombination.addListener((observable, oldValue, newValue) -> {
            if (null == oldValue) {
                combination.setValue(newValue);
                updateTooltip(newValue, tooltip);
            }
            updateTooltip(newValue, tooltip);
        });
        updateTooltip(defaultCombination.getValue(), tooltip);
        combination.addListener((observable, oldValue, newValue) -> {
            if (null == oldValue) {
                updateTooltip(newValue, tooltip);
            }
            setText(newValue.getDisplayText());
        });
        activated.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                setText(pressKey);
            } else {
                setText(combination.getValue().getDisplayText());
            }
        });
        setEditable(false);
        setOnKeyReleased(event -> {
            if (activated.getValue()) {
                if (event.getCode().isLetterKey() || event.getCode().isDigitKey()) {
                    combination.setValue(toCombination(event));
                }
                activated.setValue(false);
            }
        });
        focusedProperty().addListener((observable, oldValue, newValue) -> activated.setValue(newValue));
        setOnMouseClicked(event -> {
            requestFocus();
            activated.setValue(true);
        });
    }

    private static KeyCodeCombination toCombination(final KeyEvent event) {
        return new KeyCodeCombination(event.getCode(),
                event.isShiftDown() ? KeyCombination.ModifierValue.DOWN : KeyCombination.ModifierValue.UP,
                event.isControlDown() ? KeyCombination.ModifierValue.DOWN : KeyCombination.ModifierValue.UP,
                event.isAltDown() ? KeyCombination.ModifierValue.DOWN : KeyCombination.ModifierValue.UP,
                event.isMetaDown() ? KeyCombination.ModifierValue.DOWN : KeyCombination.ModifierValue.UP,
                event.isShortcutDown() ? KeyCombination.ModifierValue.DOWN : KeyCombination.ModifierValue.UP);
    }

    public ReadOnlyProperty<Boolean> activatedProperty() {
        return activated;
    }

    public ReadOnlyProperty<KeyCodeCombination> combinationProperty() {
        return combination;
    }

    public Property<KeyCodeCombination> defaultCombinationProperty() {
        return defaultCombination;
    }

    public Boolean getActivated() {
        return activated.getValue();
    }

    public KeyCodeCombination getCombination() {
        return combination.getValue();
    }

    public KeyCodeCombination getDefaultCombination() {
        return defaultCombination.getValue();
    }

    public void setDefaultCombination(final KeyCodeCombination defaultCombination) {
        this.defaultCombination.setValue(defaultCombination);
    }

    public void reset() {
        combination.setValue(defaultCombination.getValue());
        activated.setValue(false);
    }

    private void updateTooltip(final KeyCodeCombination keyCodeCombination, final String tooltipString) {
        Optional.ofNullable(keyCodeCombination).ifPresent(newKeyCode ->
                setTooltip(new Tooltip(tooltipString + ' ' + newKeyCode.getDisplayText())));
    }
}
