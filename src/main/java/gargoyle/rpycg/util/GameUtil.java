package gargoyle.rpycg.util;

import java.nio.file.Files;
import java.nio.file.Path;

public final class GameUtil {
    private GameUtil() {
        throw new IllegalStateException(GameUtil.class.getName());
    }

    public static boolean isGameDirectory(Path path) {
        return path != null &&
                Files.isDirectory(path) &&
                Files.isDirectory(path.resolve("renpy")) &&
                Files.isDirectory(path.resolve("game")) &&
                Files.isDirectory(path.resolve("lib"));
    }
}
