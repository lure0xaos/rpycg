package gargoyle.fx

import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.Collections
import java.util.Enumeration
import java.util.Properties
import java.util.TreeSet
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.reader
import kotlin.io.path.writer

internal class FxFilePreferencesStore {

    private class SortedProperties(map: Map<String, String>) : Properties(null) {
        init {
            putAll(LinkedHashMap(map))
        }

        override fun keys(): Enumeration<Any> =
            Collections.enumeration(TreeSet { o1: Any, o2: Any ->
                java.lang.String.CASE_INSENSITIVE_ORDER.compare(o1.toString(), o2.toString())
            }.also<TreeSet<Any>> { it.addAll(keys) })

        override val entries: MutableSet<MutableMap.MutableEntry<Any, Any>>
            get() = TreeSet<MutableMap.MutableEntry<Any, Any>> { key1, key2 ->
                java.lang.String.CASE_INSENSITIVE_ORDER.compare(key1.toString(), key2.toString())
            }.also { it.addAll(super.entries) }
    }

    companion object {
        fun load(path: Path, map: MutableMap<String, String>) {
            with((Properties())) {
                if (path.exists() && path.isRegularFile()) {
                    path.reader(Charsets.UTF_8).use { load(it) }
                    forEach { (key, value) -> map[key.toString()] = value.toString() }
                }
            }
        }

        fun save(path: Path, map: Map<String, String>, comments: String) =
            path.writer(Charsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
                .use { SortedProperties(map).store(it, comments) }
    }
}
