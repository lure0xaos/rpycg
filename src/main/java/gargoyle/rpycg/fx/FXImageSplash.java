package gargoyle.rpycg.fx;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Window;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;

public final class FXImageSplash implements FXSplash {
    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);
    private static final Logger log = LoggerFactory.getLogger(FXImageSplash.class);

    @Override
    @NotNull
    public Window createWindow(@NotNull URL location, @Nullable ResourceBundle resources) {
        JWindow window = new JWindow();
        Container pane = window.getContentPane();
        pane.setLayout(new BorderLayout());
        JLabel label = new JLabel(new ImageIcon(location));
        label.setBackground(TRANSPARENT);
        pane.add(label, BorderLayout.CENTER);
        window.setBackground(TRANSPARENT);
        return window;
    }

    @Override
    public void handleSplashNotification(@NotNull FXSplashNotification splashNotification) {
        log.info(MessageFormat.format("{0}.{1}:{2}({3})", splashNotification.getClass().getSimpleName(),
                splashNotification.getType(), splashNotification.getProgress(), splashNotification.getDetails()));
    }
}
