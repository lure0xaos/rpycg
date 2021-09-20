package gargoyle.rpycg.util;

import java.nio.file.Files;
import java.nio.file.Path;

public final class GameUtil {
    private GameUtil() {
        throw new IllegalStateException(GameUtil.class.getName());
    }

    public static boolean isGameDirectory(final Path path) {
        return null != path &&
                Files.isDirectory(path) &&
                Files.isDirectory(path.resolve("renpy")) &&
                Files.isDirectory(path.resolve("game")) &&
                Files.isDirectory(path.resolve("lib"));
    }
}
