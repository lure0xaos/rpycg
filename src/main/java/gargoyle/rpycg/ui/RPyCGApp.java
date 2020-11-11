package gargoyle.rpycg.ui;

import gargoyle.rpycg.ex.AppUserException;
import gargoyle.rpycg.fx.FXApplication;
import gargoyle.rpycg.fx.FXComponent;
import gargoyle.rpycg.fx.FXLoad;
import javafx.scene.Parent;
import org.jetbrains.annotations.NotNull;

public final class RPyCGApp implements FXApplication {

    @NotNull
    public FXComponent<?, ? extends Parent> doStart() {
        return FXLoad.loadComponent(Main.class)
                .orElseThrow(() -> new AppUserException(AppUserException.LC_ERROR_NO_VIEW, getClass().getName()));
    }
}
