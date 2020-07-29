package gargoyle.rpycg.ui;

import gargoyle.rpycg.ex.AppException;
import gargoyle.rpycg.fx.FXContextFactory;
import gargoyle.rpycg.fx.FXLoad;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.GridPane;

public final class About extends GridPane {
    @FXML
    private Hyperlink link;

    public About() {
        FXLoad.loadComponent(FXContextFactory.currentContext(), FXLoad.getBaseName(getClass()), this, this)
                .orElseThrow(() -> new AppException("Error loading " + getClass()));
    }

    @FXML
    void onLink(ActionEvent e) {
        Object userData = link.getUserData();
        if (userData instanceof String) {
            HostServices services = FXContextFactory.currentContext().getHostServices();
            if (services != null) {
                services.showDocument((String) userData);
            }
        }
    }
}
