package gargoyle.rpycg.util

import java.nio.file.Files
import java.nio.file.Path

object GameUtil {

    fun isGameDirectory(path: Path?): Boolean =
        null != path &&
                Files.isDirectory(path) &&
                Files.isDirectory(path.resolve("renpy")) &&
                Files.isDirectory(path.resolve("game")) &&
                Files.isDirectory(path.resolve("lib"))

}
