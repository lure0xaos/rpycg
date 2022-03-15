package gargoyle.rpycg.ui.icons;

import gargoyle.fx.FXConstants;
import gargoyle.fx.FXContext;

import java.net.URL;
import java.util.Optional;

public enum Icon {

    COMPUTER("computer"),
    DELETE("clear"),
    EMPTY("empty"),
    FILE("text-x-generic"),
    FOLDER("folder"),
    FOLDER_OPEN("folder-open"),
    GAME_FOLDER("game-folder"),
    GAME_FOLDER_OPEN("game-folder-open"),
    MENU("menu"),
    TEMPLATE("template"),
    VARIABLE("var");

    private final String value;

    Icon(final String value) {
        this.value = value;
    }

    public Optional<URL> findIcon(final FXContext context) {
        return context.findResource(Icon.class, value, FXConstants.EXT__IMAGES);
    }

}
