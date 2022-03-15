package gargoyle.fx;

import gargoyle.fx.log.FXLog;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Window;
import java.net.URL;
import java.util.Map;
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
        FXLog.info("{name}.{type}:{progress}({details})",
                Map.of("name", splashNotification.getClass().getSimpleName(),
                        "type", splashNotification.getType(),
                        "progress", splashNotification.getProgress(),
                        "details", splashNotification.getDetails())
        );
    }
}
