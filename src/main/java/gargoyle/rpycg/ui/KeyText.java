package gargoyle.rpycg.ui;

import gargoyle.rpycg.fx.FXContextFactory;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import java.util.Optional;
import java.util.ResourceBundle;

@DefaultProperty("keyCode")
public final class KeyText extends TextField {
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.KeyText")
    private static final String LC_DEFAULT = "default-is";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.KeyText")
    private static final String LC_PRESS_ANY_KEY = "press-any-key";
    @NotNull
    private final Property<Boolean> activated;
    @NotNull
    private final Property<KeyCodeCombination> combination;
    @NotNull
    private final Property<KeyCodeCombination> defaultCombination;

    public KeyText() {
        defaultCombination = new SimpleObjectProperty<>(null);
        combination = new SimpleObjectProperty<>(null);
        activated = new SimpleBooleanProperty(false);
        Optional<ResourceBundle> optional = FXContextFactory.currentContext().loadResources(KeyText.class);
        String tooltip = optional.map(resources -> resources.getString(LC_DEFAULT)).orElse(LC_DEFAULT);
        String pressKey = optional.map(resources -> resources.getString(LC_PRESS_ANY_KEY)).orElse(LC_PRESS_ANY_KEY);
        defaultCombination.addListener((observable, oldValue, newValue) -> {
            if (oldValue == null) {
                combination.setValue(newValue);
                updateTooltip(newValue, tooltip);
            }
            updateTooltip(newValue, tooltip);
        });
        updateTooltip(defaultCombination.getValue(), tooltip);
        combination.addListener((observable, oldValue, newValue) -> {
            if (oldValue == null) {
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

    private void updateTooltip(@Nullable KeyCodeCombination keyCodeCombination, @NotNull String tooltipString) {
        Optional.ofNullable(keyCodeCombination).ifPresent(newKeyCode ->
                setTooltip(new Tooltip(tooltipString + ' ' + newKeyCode.getDisplayText())));
    }

    @NotNull
    private static KeyCodeCombination toCombination(@NotNull KeyEvent event) {
        return new KeyCodeCombination(event.getCode(),
                event.isShiftDown() ? KeyCombination.ModifierValue.DOWN : KeyCombination.ModifierValue.UP,
                event.isControlDown() ? KeyCombination.ModifierValue.DOWN : KeyCombination.ModifierValue.UP,
                event.isAltDown() ? KeyCombination.ModifierValue.DOWN : KeyCombination.ModifierValue.UP,
                event.isMetaDown() ? KeyCombination.ModifierValue.DOWN : KeyCombination.ModifierValue.UP,
                event.isShortcutDown() ? KeyCombination.ModifierValue.DOWN : KeyCombination.ModifierValue.UP);
    }

    @NotNull
    public ReadOnlyProperty<Boolean> activatedProperty() {
        return activated;
    }

    @NotNull
    public ReadOnlyProperty<KeyCodeCombination> combinationProperty() {
        return combination;
    }

    @NotNull
    public Property<KeyCodeCombination> defaultCombinationProperty() {
        return defaultCombination;
    }

    @NotNull
    public Boolean getActivated() {
        return activated.getValue();
    }

    @NotNull
    public KeyCodeCombination getCombination() {
        return combination.getValue();
    }

    @NotNull
    public KeyCodeCombination getDefaultCombination() {
        return defaultCombination.getValue();
    }

    public void setDefaultCombination(@NotNull KeyCodeCombination defaultCombination) {
        this.defaultCombination.setValue(defaultCombination);
    }

    public void reset() {
        combination.setValue(defaultCombination.getValue());
        activated.setValue(false);
    }
}
