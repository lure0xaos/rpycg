package gargoyle.rpycg.ui;

import gargoyle.rpycg.ex.AppException;
import gargoyle.rpycg.fx.FXContextFactory;
import gargoyle.rpycg.fx.FXLoad;
import javafx.scene.layout.BorderPane;

public final class Banner extends BorderPane {

    public Banner() {
        FXLoad.loadComponent(FXContextFactory.currentContext(), FXLoad.getBaseName(getClass()), this, this)
                .orElseThrow(() -> new AppException("Error loading " + getClass()));
    }
}
