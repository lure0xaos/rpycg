package gargoyle.rpycg.fx;

import javafx.scene.Parent;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("RedundantThrows")
public interface FXApplication {
    static void notifySplash(FXSplash.FXSplashNotification splashNotification) {
        FXLauncher.notifySplash(splashNotification);
    }

    default void doInit() throws Exception {
    }

    @NotNull
    FXComponent<?, ? extends Parent> doStart() throws Exception;

    default void doStop() throws Exception {
    }
}
