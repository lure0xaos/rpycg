package gargoyle.rpycg.ui;

import gargoyle.rpycg.ex.AppUserException;
import gargoyle.rpycg.fx.FXContextFactory;
import gargoyle.rpycg.fx.FXLoad;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public final class About extends GridPane {
    @FXML
    private Hyperlink link;

    public About() {
        FXLoad.loadComponent(this)
                .orElseThrow(() -> new AppUserException(AppUserException.LC_ERROR_NO_VIEW, getClass().getName()));
    }

    @FXML
    void onLink(ActionEvent e) {
        Optional.ofNullable(FXContextFactory.currentContext().getHostServices())
                .ifPresent(hostServices -> hostServices.showDocument(String.valueOf(link.getUserData())));
    }
}
