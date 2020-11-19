package gargoyle.rpycg.ui;

import gargoyle.rpycg.ex.AppUserException;
import gargoyle.rpycg.fx.FXContextFactory;
import gargoyle.rpycg.fx.FXDialogs;
import gargoyle.rpycg.fx.FXRun;
import gargoyle.rpycg.fx.FXUserException;
import gargoyle.rpycg.fx.FXUtil;
import gargoyle.rpycg.service.Validator;
import gargoyle.rpycg.ui.model.DisplayItem;
import gargoyle.rpycg.util.Check;
import gargoyle.rpycg.util.Classes;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public final class MenuDialog extends Dialog<DisplayItem> implements Initializable {
    private static final String CLASS_DANGER = "danger";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.MenuDialog")
    private static final String LC_CANCEL = "cancel";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.MenuDialog")
    private static final String LC_ERROR_EMPTY = "error.empty";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.MenuDialog")
    private static final String LC_ERROR_FORMAT = "error.format";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.MenuDialog")
    private static final String LC_ERROR_UNIQUE = "error.unique";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.MenuDialog")
    private static final String LC_OK_CREATE = "ok_create";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.MenuDialog")
    private static final String LC_OK_EDIT = "ok_edit";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.MenuDialog")
    private static final String LC_TITLE_CREATE = "title_create";
    @PropertyKey(resourceBundle = "gargoyle.rpycg.ui.MenuDialog")
    private static final String LC_TITLE_EDIT = "title_edit";

    private final SimpleObjectProperty<DisplayItem> displayItem;
    private final SimpleBooleanProperty edit;
    private final ObservableList<String> known;
    @FXML
    private TextField label;
    @FXML
    private TextField name;
    private Validator validator;

    public MenuDialog() {
        edit = new SimpleBooleanProperty(false);
        displayItem = new SimpleObjectProperty<>(DisplayItem.createMenu("", ""));
        known = FXCollections.observableArrayList();
        FXContextFactory.currentContext().loadDialog(this)
                .orElseThrow(() -> new AppUserException(AppUserException.LC_ERROR_NO_VIEW, MenuDialog.class.getName()));
    }

    @NotNull
    public SimpleObjectProperty<DisplayItem> displayItemProperty() {
        return displayItem;
    }

    @NotNull
    public SimpleBooleanProperty editProperty() {
        return edit;
    }

    @Nullable
    public DisplayItem getDisplayItem() {
        return displayItem.getValue();
    }

    public void setDisplayItem(@NotNull DisplayItem displayItem) {
        this.displayItem.setValue(displayItem);
    }

    @NotNull
    public List<String> getKnown() {
        return Collections.unmodifiableList(known);
    }

    public void setKnown(@NotNull Collection<String> value) {
        known.setAll(value);
    }

    @SuppressWarnings("ReturnOfNull")
    @Override
    public void initialize(@NotNull URL location, @Nullable ResourceBundle resources) {
        FXUtil.requireNonNull(resources, FXUserException.LC_ERROR_NO_RESOURCES, location.toExternalForm());
        FXDialogs.decorateDialog(this,
                buttonType -> buttonType.getButtonData().isCancelButton() ? null :
                        DisplayItem.createMenu(label.getText(), name.getText()), Map.of(
                        ButtonBar.ButtonData.OK_DONE, createOk(resources, edit.getValue()),
                        ButtonBar.ButtonData.CANCEL_CLOSE, resources.getString(LC_CANCEL)
                ), createTitle(resources, edit.getValue()));
        edit.addListener((observable, oldValue, newValue) -> {
            setTitle(createTitle(resources, newValue));
            getDialogPane().getButtonTypes().replaceAll(buttonType -> buttonType.getButtonData().isDefaultButton() ?
                    new ButtonType(createOk(resources, newValue), ButtonBar.ButtonData.OK_DONE) : buttonType);
            onShow();
        });
        displayItem.addListener((observable, oldValue, newValue) -> {
            edit.setValue(!newValue.getName().isBlank());
            label.setText(newValue.getLabel());
            name.setText(newValue.getName());
        });
        label.textProperty().addListener((observable, oldValue, newValue) -> displayItem.getValue().setLabel(newValue));
        name.textProperty().addListener((observable, oldValue, newValue) -> displayItem.getValue().setName(newValue));
        validator = new Validator();
        validator.addValidator(name.textProperty(), s -> s.isBlank() ? resources.getString(LC_ERROR_EMPTY) : null,
                errors -> decorateError(name, errors));
        validator.addValidator(name.textProperty(), s -> Check.isIdentifier(s) ? null
                : resources.getString(LC_ERROR_FORMAT), errors -> decorateError(name, errors));
        validator.addValidator(name.textProperty(), s -> known.contains(s) ? resources.getString(LC_ERROR_UNIQUE)
                : null, errors -> decorateError(name, errors));
        validator.addValidator(label.textProperty(), s -> Check.isText(s) ? null
                : resources.getString(LC_ERROR_FORMAT), errors -> decorateError(label, errors));
        validator.validProperty().addListener((observable, oldValue, newValue) ->
                getDialogPane().getButtonTypes().filtered(buttonType -> buttonType.getButtonData().isDefaultButton())
                        .forEach(buttonType -> getDialogPane().lookupButton(buttonType).setDisable(!newValue)));
        onShownProperty().addListener((observable, oldValue, newValue) -> onShow());
        FXRun.runLater(this::onShow);
    }

    @NotNull
    private static String createOk(@NotNull ResourceBundle resources, @NotNull Boolean newValue) {
        return resources.getString(newValue ? LC_OK_EDIT : LC_OK_CREATE);
    }

    @NotNull
    private static String createTitle(@NotNull ResourceBundle resources, @NotNull Boolean value) {
        return resources.getString(value ? LC_TITLE_EDIT : LC_TITLE_CREATE);
    }

    private void onShow() {
        validator.validate();
        name.requestFocus();
    }

    private static void decorateError(@NotNull Control cell, @NotNull Collection<String> errors) {
        if (errors.isEmpty()) {
            Classes.classRemove(cell, CLASS_DANGER);
            cell.setTooltip(null);
        } else {
            Classes.classAdd(cell, CLASS_DANGER);
            cell.setTooltip(new Tooltip(String.join("\n", errors)));
        }
    }

    public boolean isEdit() {
        return edit.getValue();
    }

    public void setEdit(boolean edit) {
        this.edit.setValue(edit);
    }
}
