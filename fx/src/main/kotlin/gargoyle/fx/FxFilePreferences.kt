package gargoyle.fx

import java.nio.file.Path
import java.nio.file.Paths
import java.util.Collections
import java.util.TreeMap
import java.util.prefs.AbstractPreferences
import kotlin.io.path.deleteIfExists
import kotlin.io.path.listDirectoryEntries

internal class FxFilePreferences private constructor(
    private val userRoot: Boolean,
    name: String,
    parent: AbstractPreferences? = null
) : AbstractPreferences(parent, name) {

    private val properties: MutableMap<String, String> =
        Collections.synchronizedSortedMap(TreeMap { obj: String, str: String -> obj.compareTo(str, ignoreCase = true) })

    override fun putSpi(key: String, value: String) {
        properties[key] = value
        flush()
    }

    override fun getSpi(key: String): String =
        properties[key]!!

    override fun removeSpi(key: String) {
        properties.remove(key)
        flush()
    }

    override fun removeNodeSpi() {
        filePath.deleteIfExists()
    }

    override fun keysSpi(): Array<String> =
        properties.keys.toTypedArray()

    override fun childrenNamesSpi(): Array<String> =
        rootPath.listDirectoryEntries("*.$SUFFIX").map { it.fileName.toString().substringBeforeLast(SUFFIX) }
            .toTypedArray()

    override fun childSpi(name: String): AbstractPreferences =
        FxFilePreferences(userRoot, name, this)

    override fun syncSpi() =
        FxFilePreferencesStore.load(filePath, properties)

    override fun flushSpi() =
        FxFilePreferencesStore.save(filePath, properties, absolutePath())

    private val filePath: Path
        get() =
            rootPath.resolve(
                if (null == parent()) SUFFIX
                else "${("${parent().absolutePath()}/${name()}").replace('/', '.')}$SUFFIX"
            )
    private val rootPath: Path
        get() = if (userRoot) USER_ROOT_PATH else SYSTEM_ROOT_PATH

    companion object {
        private const val SUFFIX = ".prefs"
        private val SYSTEM_ROOT_PATH =
            FxIntUtil.doPrivileged<Path> { Paths.get(System.getProperty("user.home")) }
        private val USER_ROOT_PATH =
            FxIntUtil.doPrivileged<Path> { Paths.get(System.getProperty("user.home")) }
        val systemRoot = lazy { FxFilePreferences(false, "", null) }
        val userRoot = lazy { FxFilePreferences(true, "", null) }
    }

    init {
        syncSpi()
    }
}
