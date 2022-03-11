package gargoyle.fx

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.Collections
import java.util.Enumeration
import java.util.Properties
import java.util.SortedSet
import java.util.TreeSet

internal class FxFilePreferencesStore {

    private class SortedProperties(map: Map<String, String>) : Properties(null) {
        init {
            putAll(LinkedHashMap(map))
        }

        override fun keys(): Enumeration<Any> {
            val set: MutableCollection<Any> = TreeSet { o1: Any, o2: Any ->
                java.lang.String.CASE_INSENSITIVE_ORDER.compare(o1.toString(), o2.toString())
            }
            set.addAll(keys)
            return Collections.enumeration(set)
        }

        override val entries: MutableSet<MutableMap.MutableEntry<Any, Any>>
            get() {
                val set: SortedSet<MutableMap.MutableEntry<Any, Any>> =
                    TreeSet { (key): Map.Entry<Any, Any>, (key1): Map.Entry<Any, Any> ->
                        java.lang.String.CASE_INSENSITIVE_ORDER.compare(
                            key.toString(), key1.toString()
                        )
                    }
                set.addAll(super.entries)
                return set.toMutableSet()
            }
    }

    companion object {
        private val CHARSET = StandardCharsets.UTF_8
        fun load(path: Path, map: MutableMap<String, String>) {
            val properties = Properties()
            if (Files.exists(path) && Files.isRegularFile(path)) {
                Files.newBufferedReader(path, CHARSET).use { properties.load(it) }
                properties.forEach { (key, value) -> map[key.toString()] = value.toString() }
            }
        }

        fun save(path: Path, map: Map<String, String>, comments: String) =
            Files.newBufferedWriter(path, CHARSET, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
                .use { SortedProperties(map).store(it, comments) }
    }
}
