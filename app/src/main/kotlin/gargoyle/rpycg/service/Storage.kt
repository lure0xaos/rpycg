package gargoyle.rpycg.service

import gargoyle.rpycg.ex.AppException
import gargoyle.rpycg.model.ModelItem
import javafx.beans.property.Property
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.readLines
import kotlin.io.path.writeText

class Storage {
    private val converter: ScriptConverter = ScriptConverter()
    private val gamePath: Property<Path> = SimpleObjectProperty()
    private val modified: Property<Boolean> = SimpleBooleanProperty(false)
    private val path: Property<Path> = SimpleObjectProperty()

    fun gamePathProperty(): Property<Path> =
        gamePath

    fun getGamePath(): Path =
        gamePath.value

    fun setGamePath(path: Path) {
        gamePath.value = path
    }

    fun getModified(): Boolean = modified.value

    fun setModified(modified: Boolean) {
        this.modified.value = modified
    }

    fun getPath(): Path? =
        path.value

    fun setPath(path: Path) {
        this.path.value = path
    }

    fun load(loadPath: Path): ModelItem {
        path.value = loadPath
        return reload()
    }

    fun modifiedProperty(): Property<Boolean> = modified

    fun pathProperty(): Property<Path> = path

    fun saveAs(savePath: Path, root: ModelItem) {
        path.value = savePath
        save(root)
    }

    private fun reload(): ModelItem {
        return try {
            converter.fromScript(path.value.readLines()).also {
                modified.value = false
            }
        } catch (e: IOException) {
            modified.value = true
            throw AppException("Load error $path", e)
        }
    }

    private fun save(root: ModelItem) {
        try {
            path.value.writeText(converter.toScript(root).joinToString(System.lineSeparator())).also {
                modified.value = false
            }
        } catch (e: IOException) {
            throw AppException("Save error $path", e)
        }
    }
}
