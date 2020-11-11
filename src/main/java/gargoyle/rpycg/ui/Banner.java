package gargoyle.rpycg.ui;

import gargoyle.rpycg.ex.AppUserException;
import gargoyle.rpycg.fx.FXLoad;
import javafx.scene.layout.BorderPane;

public final class Banner extends BorderPane {

    public Banner() {
        FXLoad.loadComponent(this)
                .orElseThrow(() -> new AppUserException(AppUserException.LC_ERROR_NO_VIEW, getClass().getName()));
    }
}
