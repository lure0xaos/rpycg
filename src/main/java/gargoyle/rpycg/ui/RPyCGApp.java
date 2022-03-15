package gargoyle.rpycg.ui;

import gargoyle.rpycg.ex.AppUserException;
import gargoyle.rpycg.fx.FXApplication;
import gargoyle.rpycg.fx.FXComponent;
import gargoyle.rpycg.fx.FXContextFactory;
import javafx.scene.Parent;

public final class RPyCGApp implements FXApplication {
    public FXComponent<?, ? extends Parent> doStart() {
        return FXContextFactory.currentContext().loadComponent(Main.class)
                .orElseThrow(() -> new AppUserException(AppUserException.LC_ERROR_NO_VIEW, RPyCGApp.class.getName()));
    }
}
