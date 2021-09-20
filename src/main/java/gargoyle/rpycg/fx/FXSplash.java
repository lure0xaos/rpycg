package gargoyle.rpycg.fx;

import java.awt.Window;
import java.net.URL;
import java.util.ResourceBundle;

@FunctionalInterface
public interface FXSplash {

    Window createWindow(URL location, ResourceBundle resources);

    default void handleSplashNotification(final FXSplashNotification splashNotification) {
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
