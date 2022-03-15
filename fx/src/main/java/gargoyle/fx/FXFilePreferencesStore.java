package gargoyle.fx;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
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

final class FXFilePreferencesStore {
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private FXFilePreferencesStore() {
        throw new IllegalStateException(FXFilePreferencesStore.class.getName());
    }

    public static synchronized void load(final Path path, final Map<? super String, ? super String> map) throws IOException {
        final Properties properties = new Properties();
        if (Files.exists(path) && Files.isRegularFile(path)) {
            try (final BufferedReader reader = Files.newBufferedReader(path, CHARSET)) {
                properties.load(reader);
            }
            for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
                map.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            }
        }
    }

    public static synchronized void save(final Path path, final Map<String, String> map,
                                         final String comments) throws IOException {
        try (final BufferedWriter writer = Files.newBufferedWriter(path, CHARSET,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            new SortedProperties(map).store(writer, comments);
        }
    }

    @SuppressWarnings({"CloneableClassInSecureContext",
            "ClassExtendsConcreteCollection"})
    private static final class SortedProperties extends Properties {
        @Serial
        private static final long serialVersionUID = -1;

        private SortedProperties(final Map<String, String> map) {
            super(null);
            putAll(new LinkedHashMap<>(map));
        }

        @Override
        public Enumeration<Object> keys() {
            final Collection<Object> set = new TreeSet<>((o1, o2) ->
                    String.CASE_INSENSITIVE_ORDER.compare(o1.toString(), o2.toString()));
            set.addAll(keySet());
            return Collections.enumeration(set);
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public Set<Map.Entry<Object, Object>> entrySet() {
            final SortedSet<Map.Entry<Object, Object>> set = new TreeSet<>((o1, o2) ->
                    String.CASE_INSENSITIVE_ORDER.compare(o1.getKey().toString(), o2.getKey().toString()));
            set.addAll(super.entrySet());
            return Collections.unmodifiableSortedSet(set);
        }

        @Serial
        private void readObject(final ObjectInputStream s) {
            throw new IllegalStateException(SortedProperties.class.getName());
        }

        @Serial
        private void writeObject(final ObjectOutputStream s) {
            throw new IllegalStateException(SortedProperties.class.getName());
        }
    }
}
