package gargoyle.rpycg.fx;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Stream;

final class FXFilePreferences extends AbstractPreferences {
    private static final String MSG_LOAD_ERROR = "Load error {0}";
    private static final String MSG_SAVE_ERROR = "Save error {0}";
    private static final String[] STRINGS = new String[0];
    private static final String SUFFIX = ".prefs";
    @SuppressWarnings("AccessOfSystemProperties")
    private static final Path SYSTEM_ROOT_PATH = AccessController.doPrivileged((PrivilegedAction<Path>)
            () -> Paths.get(System.getProperty("user.home")));
    @SuppressWarnings("AccessOfSystemProperties")
    private static final Path USER_ROOT_PATH = AccessController.doPrivileged((PrivilegedAction<Path>)
            () -> Paths.get(System.getProperty("user.home")));
    private static final FXHolder<Preferences> systemRoot = new FXHolder<>(() ->
            new FXFilePreferences(false, null, ""));
    private static final FXHolder<Preferences> userRoot = new FXHolder<>(() ->
            new FXFilePreferences(true, null, ""));
    private final boolean isUserRoot;
    private final Map<String, String> properties;

    private FXFilePreferences(final boolean isUserRoot, final AbstractPreferences parent, final String name) {
        super(parent, name);
        this.isUserRoot = isUserRoot;
        properties = Collections.synchronizedSortedMap(new TreeMap<>(String::compareToIgnoreCase));
        try {
            syncSpi();
        } catch (final BackingStoreException e) {
            throw new FXException(MessageFormat.format(MSG_LOAD_ERROR, FXFilePreferences.class.getName()), e);
        }
    }

    static Preferences getSystemRoot() {
        return systemRoot.get();
    }

    static Preferences getUserRoot() {
        return userRoot.get();
    }

    @Override
    protected void putSpi(final String key, final String value) {
        properties.put(key, value);
        try {
            flush();
        } catch (final BackingStoreException e) {
            throw new FXException(MessageFormat.format(MSG_SAVE_ERROR, FXFilePreferences.class.getName()), e);
        }
    }

    @Override
    protected String getSpi(final String key) {
        return properties.get(key);
    }

    @Override
    protected void removeSpi(final String key) {
        properties.remove(key);
        try {
            flush();
        } catch (final BackingStoreException e) {
            throw new FXException(MessageFormat.format(MSG_SAVE_ERROR, FXFilePreferences.class.getName()), e);
        }
    }

    @Override
    protected void removeNodeSpi() throws BackingStoreException {
        try {
            Files.delete(getFilePath());
        } catch (final IOException e) {
            throw new BackingStoreException(new FXException(MessageFormat.format(MSG_SAVE_ERROR,
                    FXFilePreferences.class.getName()), e));
        }
    }

    @Override
    protected String[] keysSpi() {
        return properties.keySet().toArray(STRINGS);
    }

    @Override
    protected String[] childrenNamesSpi() throws BackingStoreException {
        try (final Stream<Path> stream = Files.find(getRootPath(), 0, (path, basicFileAttributes) ->
                path.getFileName().toString().endsWith(SUFFIX))) {
            return stream.map(path -> {
                final String fileName = path.getFileName().toString();
                return fileName.substring(0, fileName.length() - SUFFIX.length());
            }).toArray(String[]::new);
        } catch (final IOException e) {
            throw new BackingStoreException(new FXException(MessageFormat.format(MSG_LOAD_ERROR,
                    FXFilePreferences.class.getName()), e));
        }
    }

    @Override
    protected AbstractPreferences childSpi(final String name) {
        return new FXFilePreferences(isUserRoot, this, name);
    }

    @Override
    protected void syncSpi() throws BackingStoreException {
        final Path filePath = getFilePath();
        try {
            FXFilePreferencesStore.load(filePath, properties);
        } catch (final IOException e) {
            throw new BackingStoreException(new FXException(MessageFormat.format(MSG_LOAD_ERROR,
                    FXFilePreferences.class.getName()), e));
        }
    }

    @Override
    protected void flushSpi() throws BackingStoreException {
        try {
            FXFilePreferencesStore.save(getFilePath(), properties, absolutePath());
        } catch (final IOException e) {
            throw new BackingStoreException(new FXException(MessageFormat.format(MSG_LOAD_ERROR,
                    FXFilePreferences.class.getName()), e));
        }
    }

    private Path getFilePath() {
        return null == parent() ? getRootPath().resolve(SUFFIX) :
                getRootPath().resolve((parent().absolutePath() + '/' + name())
                        .replace('/', '.') + SUFFIX);
    }

    private Path getRootPath() {
        return isUserRoot ? USER_ROOT_PATH : SYSTEM_ROOT_PATH;
    }
}
