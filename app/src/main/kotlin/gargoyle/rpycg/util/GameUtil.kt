package gargoyle.rpycg.util

import java.nio.file.Path
import kotlin.io.path.isDirectory

object GameUtil {

    fun isGameDirectory(path: Path?): Boolean =
        path?.isDirectory() == true &&
                path.resolve("renpy").isDirectory() &&
                path.resolve("game").isDirectory() &&
                path.resolve("lib").isDirectory()

}
