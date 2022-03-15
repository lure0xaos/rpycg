package gargoyle.rpycg.ui;

import gargoyle.fx.FXContextFactory;
import gargoyle.fx.FXRun;
import gargoyle.fx.FXUtil;
import gargoyle.rpycg.ex.AppUserException;
import gargoyle.rpycg.model.VarType;
import gargoyle.rpycg.service.Validator;
import gargoyle.rpycg.ui.model.DisplayItem;
import gargoyle.rpycg.util.Check;
import gargoyle.rpycg.util.Classes;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.ResourceBundle;

public final class VariableDialog extends Dialog<DisplayItem> implements Initializable {
    private static final String CLASS_DANGER = "danger";
    private static final String LC_CANCEL = "cancel";
    private static final String LC_ERROR_EMPTY = "error.empty";
    private static final String LC_ERROR_FORMAT = "error.format";
    private static final String LC_OK_CREATE = "ok_create";
    private static final String LC_OK_EDIT = "ok_edit";
    private static final String LC_TITLE_CREATE = "title_create";
    private static final String LC_TITLE_EDIT = "title_edit";
    private final SimpleObjectProperty<DisplayItem> displayItem;
    private final SimpleBooleanProperty edit;
    @FXML
    private TextField label;
    @FXML
    private TextField name;
    @FXML
    private ComboBox<VarType> type;
    private Validator validator;
    @FXML
    private TextField value;

    public VariableDialog() {
        edit = new SimpleBooleanProperty(false);
        displayItem = new SimpleObjectProperty<>(DisplayItem.createVariable(VarType.INT, "", "", "0"));
        FXContextFactory.currentContext().loadDialog(this)
                .orElseThrow(() ->
                        new AppUserException("No view {resource}",
                                Map.of("resource", VariableDialog.class.getName())));
    }

    private static String createOk(final ResourceBundle resources, final Boolean newValue) {
        return resources.getString(newValue ? LC_OK_EDIT : LC_OK_CREATE);
    }

    private static String createTitle(final ResourceBundle resources, final Boolean value) {
        return resources.getString(value ? LC_TITLE_EDIT : LC_TITLE_CREATE);
    }

    private static void decorateError(final Control cell, final Collection<String> errors) {
        if (errors.isEmpty()) {
            Classes.classRemove(cell, CLASS_DANGER);
            cell.setTooltip(null);
        } else {
            Classes.classAdd(cell, CLASS_DANGER);
            cell.setTooltip(new Tooltip(String.join("\n", errors)));
        }
    }

    public SimpleObjectProperty<DisplayItem> displayItemProperty() {
        return displayItem;
    }

    public SimpleBooleanProperty editProperty() {
        return edit;
    }

    public DisplayItem getDisplayItem() {
        return displayItem.getValue();
    }

    public void setDisplayItem(final DisplayItem displayItem) {
        this.displayItem.setValue(displayItem);
    }

    @SuppressWarnings("ReturnOfNull")
    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        FXUtil.requireNonNull(resources, "No resources {location}",
                Map.of("location", location.toExternalForm()));
        FXContextFactory.currentContext().decorateDialog(this,
                buttonType -> buttonType.getButtonData().isCancelButton() ? null :
                        DisplayItem.createVariable(type.getValue(),
                                label.getText(), name.getText(), value.getText()), Map.of(
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
            type.setValue(newValue.getType());
            label.setText(newValue.getLabel());
            name.setText(newValue.getName());
            value.setText(newValue.getValue());
        });
        type.valueProperty().addListener((observable, oldValue, newValue) -> displayItem.getValue().setType(newValue));
        label.textProperty().addListener((observable, oldValue, newValue) -> displayItem.getValue().setLabel(newValue));
        name.textProperty().addListener((observable, oldValue, newValue) -> displayItem.getValue().setName(newValue));
        value.textProperty().addListener((observable, oldValue, newValue) -> displayItem.getValue().setValue(newValue));
        type.getItems().setAll(VarType.values());
        type.getSelectionModel().select(VarType.INT);
        type.valueProperty().addListener((observable, oldValue, newValue) -> validator.validate());
        validator = new Validator();
        validator.addValidator(name.textProperty(), s -> s.isBlank() ? resources.getString(LC_ERROR_EMPTY) : null,
                errors -> decorateError(name, errors));
        validator.addValidator(name.textProperty(), s -> Check.isIdentifier(s) ? null
                : resources.getString(LC_ERROR_FORMAT), errors -> decorateError(name, errors));
        validator.addValidator(label.textProperty(), s -> Check.isText(s) ? null
                : resources.getString(LC_ERROR_FORMAT), errors -> decorateError(label, errors));
        validator.addValidator(value.textProperty(), s ->
                isEmpty(s) ||
                        isMatchFormat(s, type.getSelectionModel().getSelectedItem())
                        ? null : resources.getString(LC_ERROR_FORMAT), errors -> decorateError(value, errors));
        validator.validProperty().addListener((observable, oldValue, newValue) ->
                getDialogPane().getButtonTypes().filtered(buttonType -> buttonType.getButtonData().isDefaultButton())
                        .forEach(buttonType -> getDialogPane().lookupButton(buttonType).setDisable(!newValue)));
        onShownProperty().addListener((observable, oldValue, newValue) -> onShow());
        FXRun.runLater(this::onShow);
    }

    public boolean isEdit() {
        return edit.getValue();
    }

    public void setEdit(final boolean edit) {
        this.edit.setValue(edit);
    }

    private boolean isEmpty(final String val) {
        return null == val || val.isEmpty();
    }

    private boolean isMatchFormat(final String val, final VarType varType) {
        return switch (varType) {
            case INT -> Check.isInteger(val);
            case FLOAT -> Check.isFloat(val);
            case STR -> true;
        };
    }

    private void onShow() {
        validator.validate();
        name.requestFocus();
    }
}
