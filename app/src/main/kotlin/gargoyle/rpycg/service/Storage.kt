package gargoyle.rpycg.service

import gargoyle.rpycg.ex.AppUserException
import gargoyle.rpycg.model.ModelItem
import javafx.beans.property.Property
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

@Suppress("unused")
class Storage {
    private val converter: ScriptConverter = ScriptConverter()
    private val gamePath: Property<Path> = SimpleObjectProperty()
    private val modified: Property<Boolean> = SimpleBooleanProperty(false)
    private val path: Property<Path> = SimpleObjectProperty()

    fun gamePathProperty(): Property<Path> = gamePath

    fun getGamePath(): Path = gamePath.value

    fun setGamePath(path: Path) {
        gamePath.value = path
    }

    fun getModified(): Boolean = modified.value

    fun setModified(modified: Boolean) {
        this.modified.value = modified
    }

    fun getPath(): Path? = path.value

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
            val item = converter.fromScript(Files.readAllLines(path.value))
            modified.value = false
            item
        } catch (e: IOException) {
            modified.value = true
            throw AppUserException(e, "Load error $path")
        }
    }

    private fun save(root: ModelItem) {
        try {
            Files.writeString(path.value, converter.toScript(root).joinToString(System.lineSeparator()))
            modified.value = false
        } catch (e: IOException) {
            throw AppUserException(e, "Save error $path")
        }
    }
}
