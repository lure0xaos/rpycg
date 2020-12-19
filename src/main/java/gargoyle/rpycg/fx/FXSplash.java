package gargoyle.rpycg.fx;

import java.awt.Window;
import java.io.Closeable;
import java.net.URL;
import java.util.ResourceBundle;

@FunctionalInterface
public interface FXSplash extends Closeable {
    default void close() {
    }

    Window createWindow(URL location, ResourceBundle resources);

    default void handleSplashNotification(FXSplashNotification splashNotification) {
    }

    interface FXSplashNotification {
        String getDetails();

        double getProgress();

        Type getType();

        enum Type {
            PRE_INIT,
            INIT,
            START,
            STOP
        }
    }
}
