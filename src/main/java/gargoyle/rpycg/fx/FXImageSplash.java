package gargoyle.rpycg.fx;

import gargoyle.rpycg.fx.log.FXLog;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

public final class FXImageSplash implements FXSplash {
    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    public FXImageSplash() {
    }

    @Override
    public Window createWindow(final URL location, final ResourceBundle resources) {
        final JWindow window = new JWindow();
        final Container pane = window.getContentPane();
        pane.setLayout(new BorderLayout());
        final JLabel label = new JLabel(new ImageIcon(location));
        label.setBackground(TRANSPARENT);
        pane.add(label, BorderLayout.CENTER);
        window.setBackground(TRANSPARENT);
        return window;
    }

    @Override
    public void handleSplashNotification(final FXSplashNotification splashNotification) {
        FXLog.info("{0}.{1}:{2}({3})", splashNotification.getClass().getSimpleName(),
                splashNotification.getType(), splashNotification.getProgress(), splashNotification.getDetails());
    }
}
