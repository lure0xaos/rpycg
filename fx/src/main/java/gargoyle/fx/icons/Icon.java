package gargoyle.fx.icons;

import gargoyle.fx.FXConstants;
import gargoyle.fx.FXContext;
import gargoyle.fx.FXUtil;
import javafx.scene.control.ButtonBar;

import java.net.URL;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

import static javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE;
import static javafx.scene.control.ButtonBar.ButtonData.NO;
import static javafx.scene.control.ButtonBar.ButtonData.OK_DONE;
import static javafx.scene.control.ButtonBar.ButtonData.YES;

public enum Icon {
    CANCEL("cancel", CANCEL_CLOSE, NO),
    EMPTY("empty"),
    OK("ok", OK_DONE, YES);

    private final String value;
    private final ButtonBar.ButtonData[] buttonData;

    Icon(final String value, final ButtonBar.ButtonData... buttonData) {
        this.value = value;
        this.buttonData = buttonData;
    }

    public static Icon find(final ButtonBar.ButtonData buttonData) {
        return Arrays.stream(values()).filter(icon ->
                        Arrays.stream(icon.buttonData).anyMatch(data -> buttonData == data)).findFirst()
                .orElse(EMPTY);
    }

    public Optional<URL> findIcon(final FXContext context) {
        final String iconBaseName = FXUtil.resolveBaseName(Icon.class, value);
        final Class<?> baseClass = context.getBaseClass();
        final Locale locale = context.getLocale();
        return FXUtil.findResource(Icon.class, locale, iconBaseName, FXConstants.EXT__IMAGES).or(() ->
                FXUtil.findResource(baseClass, locale, iconBaseName, FXConstants.EXT__IMAGES));
    }

}
