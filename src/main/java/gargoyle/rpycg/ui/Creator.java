package gargoyle.rpycg.ui;

import gargoyle.rpycg.ex.AppUserException;
import gargoyle.rpycg.fx.FXContextFactory;
import gargoyle.rpycg.fx.FXLoad;
import gargoyle.rpycg.util.Classes;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public final class Creator extends ScrollPane implements Initializable {
    private static final String CLASS_DANGER = "danger";

    private final SimpleBooleanProperty changed = new SimpleBooleanProperty(false);
    @FXML
    private TextArea source;

    public Creator() {
        FXLoad.loadComponent(FXContextFactory.currentContext(), FXLoad.getBaseName(getClass()), this, this)
                .orElseThrow(() -> new AppUserException(AppUserException.LC_ERROR_NO_VIEW, getClass().getName()));
    }

    public SimpleBooleanProperty changedProperty() {
        return changed;
    }

    public void decorateError(@NotNull Collection<String> errors) {
        if (errors.isEmpty()) {
            Classes.classRemove(source, CLASS_DANGER);
            source.setTooltip(null);
        } else {
            Classes.classAdd(source, CLASS_DANGER);
            source.setTooltip(new Tooltip(String.join("\n", errors)));
        }
    }

    @NotNull
    public List<String> getScript() {
        return Arrays.stream(source.getText().split("\n"))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    public void setScript(@NotNull Collection<String> script) {
        String text = script.stream().map(String::trim).collect(Collectors.joining("\n"));
        if (!Objects.equals(source.getText(), text)) {
            source.setText(text);
        }
    }

    @Override
    public void initialize(@NotNull URL location, @Nullable ResourceBundle resources) {
        source.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!Objects.equals(oldValue, newValue)) {
                changed.setValue(true);
                if (!source.isFocused()) {
                    Platform.runLater(() -> source.positionCaret(newValue.length()));
                }
            }
        });
    }

    public boolean isChanged() {
        return changed.getValue();
    }

    public void setChanged(boolean changed) {
        this.changed.setValue(changed);
    }

    public void onShow() {
        source.requestFocus();
    }

    public void setScriptUnforced(@NotNull Collection<String> script) {
        if (!source.isFocused()) {
            setScript(script);
        }
    }
}
