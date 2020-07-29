package gargoyle.rpycg.ui;

import gargoyle.rpycg.ex.AppException;
import gargoyle.rpycg.fx.FXContextFactory;
import gargoyle.rpycg.fx.FXDialogs;
import gargoyle.rpycg.fx.FXLoad;
import gargoyle.rpycg.ui.model.DisplayItem;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public final class SubmenuDialog extends Dialog<DisplayItem> implements Initializable {

    @FXML
    private TextField label;

    public SubmenuDialog() {
        FXLoad.loadDialog(FXContextFactory.currentContext(), this)
                .orElseThrow(() -> new AppException("Error loading " + getClass()));
    }

    @SuppressWarnings("ReturnOfNull")
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FXDialogs.decorateDialog(FXContextFactory.currentContext(), this, resources,
                param -> param.getButtonData().isCancelButton() ? null :
                         DisplayItem.createSubmenu(label.getText()));
        onShownProperty().addListener((observable, oldValue, newValue) -> label.requestFocus());
        Platform.runLater(() -> label.requestFocus());
    }
}
