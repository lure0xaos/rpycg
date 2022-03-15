package gargoyle.rpycg.ui;

import gargoyle.fx.FXContextFactory;
import gargoyle.rpycg.ex.AppUserException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.GridPane;

import java.util.Map;

public final class About extends GridPane {
    @FXML
    private Hyperlink link;

    public About() {
        FXContextFactory.currentContext().loadComponent(this)
                .orElseThrow(() -> new AppUserException("No view {resource}",
                        Map.of("resource", About.class.getName())));
    }

    @FXML
    void onLink(final ActionEvent e) {
        FXContextFactory.currentContext().showDocument(String.valueOf(link.getUserData()));
    }
}
