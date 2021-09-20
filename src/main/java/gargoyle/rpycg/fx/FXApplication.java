package gargoyle.rpycg.fx;

import javafx.scene.Parent;

@FunctionalInterface
@SuppressWarnings({"RedundantThrows", "unused", "ProhibitedExceptionDeclared"})
public interface FXApplication {
    static void notifySplash(final FXSplash.FXSplashNotification splashNotification) {
        FXLauncher.notifySplash(splashNotification);
    }

    default void doInit() throws Exception {
    }

    FXComponent<?, ? extends Parent> doStart() throws Exception;

    default void doStop() throws Exception {
    }
}
