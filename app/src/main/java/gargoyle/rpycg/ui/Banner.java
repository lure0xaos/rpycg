package gargoyle.rpycg.ui;

import gargoyle.fx.FXContextFactory;
import gargoyle.rpycg.ex.AppUserException;
import javafx.scene.layout.BorderPane;

import java.util.Map;

public final class Banner extends BorderPane {
    public Banner() {
        FXContextFactory.currentContext().loadComponent(this)
                .orElseThrow(() -> new AppUserException("No view {resource}",
                        Map.of("resource", Banner.class.getName())));
    }
}
