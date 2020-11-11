package gargoyle.rpycg.fx;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Window;
import java.io.Closeable;
import java.net.URL;
import java.util.ResourceBundle;

@FunctionalInterface
public interface FXSplash extends Closeable {

    default void close() {
    }

    @NotNull
    Window createWindow(@NotNull URL location, @Nullable ResourceBundle resources);

    default void handleSplashNotification(@NotNull FXSplashNotification splashNotification) {
    }

    interface FXSplashNotification {
        @NotNull
        String getDetails();

        double getProgress();

        @NotNull
        Type getType();

        enum Type {
            PRE_INIT,
            INIT,
            START,
            STOP
        }
    }
}
