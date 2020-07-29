package gargoyle.rpycg.fx;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

final class FilePreferencesStore {
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private FilePreferencesStore() {
        throw new IllegalStateException(FilePreferencesStore.class.getName());
    }

    public static synchronized void load(Path path, Map<String, String> map) throws IOException {
        Properties properties = new Properties();
        if (Files.exists(path) && Files.isRegularFile(path)) {
            try (BufferedReader reader = Files.newBufferedReader(path, CHARSET)) {
                properties.load(reader);
            }
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                map.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            }
        }
    }

    public static synchronized void save(Path path, Map<String, String> map,
                                         String comments) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, CHARSET,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            new SortedProperties(map).store(writer, comments);
        }
    }

    @SuppressWarnings({"CloneableClassInSecureContext",
            "ClassExtendsConcreteCollection", "CloneableClassWithoutClone"})
    private static final class SortedProperties extends Properties {
        private SortedProperties(Map<String, String> map) {
            super(null);
            new LinkedHashMap<>(map).forEach(this::put);
        }

        @Override
        public synchronized Enumeration<Object> keys() {
            Collection<Object> set = new TreeSet<>((o1, o2) ->
                    String.CASE_INSENSITIVE_ORDER.compare(o1.toString(), o2.toString()));
            set.addAll(keySet());
            return Collections.enumeration(set);
        }

        @Override
        public Set<Map.Entry<Object, Object>> entrySet() {
            SortedSet<Map.Entry<Object, Object>> set = new TreeSet<>((o1, o2) ->
                    String.CASE_INSENSITIVE_ORDER.compare(o1.getKey().toString(),o2.getKey().toString()));
            set.addAll(super.entrySet());
            return Collections.unmodifiableSortedSet(set);
        }
    }
}
