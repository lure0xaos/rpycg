package gargoyle.rpycg.ui;

import gargoyle.rpycg.ex.AppException;
import gargoyle.rpycg.fx.FXContextFactory;
import gargoyle.rpycg.fx.FXDialogs;
import gargoyle.rpycg.fx.FXLoad;
import gargoyle.rpycg.model.VarType;
import gargoyle.rpycg.ui.model.DisplayItem;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public final class NewVariableDialog extends Dialog<DisplayItem> implements Initializable {

    @FXML
    private TextField label;
    @FXML
    private TextField name;
    @FXML
    private ComboBox<VarType> type;
    @FXML
    private TextField value;

    public NewVariableDialog() {
        FXLoad.loadDialog(FXContextFactory.currentContext(), this)
                .orElseThrow(() -> new AppException("Error loading " + getClass()));
    }

    @SuppressWarnings("ReturnOfNull")
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FXDialogs.decorateDialog(FXContextFactory.currentContext(), this, resources
                , param -> param.getButtonData().isCancelButton() ? null :
                           DisplayItem.createVariable(type.getValue(),
                                   label.getText(), name.getText(), value.getText()));
        type.getItems().setAll(VarType.values());
        type.getSelectionModel().select(VarType.INT);
        Platform.runLater(() -> name.requestFocus());
    }
}
