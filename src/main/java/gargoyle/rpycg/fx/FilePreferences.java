package gargoyle.rpycg.fx;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Stream;

final class FilePreferences extends AbstractPreferences {
    @SuppressWarnings("AccessOfSystemProperties")
    public static final Path SYSTEM_ROOT_PATH = Paths.get(System.getProperty("user.home"));
    @SuppressWarnings("AccessOfSystemProperties")
    public static final Path USER_ROOT_PATH = Paths.get(System.getProperty("user.home"));
    private static final String LC_ERROR_LOAD = "error.load";
    private static final String LC_ERROR_SAVE = "error.save";
    private static final String[] STRINGS = new String[0];
    private static final String SUFFIX = ".prefs";
    private static final FXHolder<Preferences> systemRoot = new FXHolder<>(() ->
            new FilePreferences(false, null, ""));
    private static final FXHolder<Preferences> userRoot = new FXHolder<>(() ->
            new FilePreferences(true, null, ""));
    private final boolean isUserRoot;
    private final Map<String, String> properties;
    private final ResourceBundle resources;

    private FilePreferences(boolean isUserRoot, AbstractPreferences parent, String name) {
        super(parent, name);
        resources = FXContextFactory.currentContext().loadResources(FilePreferences.class)
                .orElseThrow(() ->
                        new FXUserException(FXUserException.LC_ERROR_NO_RESOURCES, FilePreferences.class.getName()));
        this.isUserRoot = isUserRoot;
        properties = Collections.synchronizedSortedMap(new TreeMap<>(String::compareToIgnoreCase));
        try {
            syncSpi();
        } catch (BackingStoreException e) {
            throw new FXException(MessageFormat.format(resources.getString(LC_ERROR_LOAD),
                    FilePreferences.class.getName()), e);
        }
    }

    private Path getFilePath() {
        return parent() == null ? getRootPath().resolve(SUFFIX) :
                getRootPath().resolve((parent().absolutePath() + '/' + name())
                        .replace('/', '.') + SUFFIX);
    }

    private Path getRootPath() {
        return isUserRoot ? USER_ROOT_PATH : SYSTEM_ROOT_PATH;
    }

    static Preferences getSystemRoot() {
        return systemRoot.get();
    }

    static Preferences getUserRoot() {
        return userRoot.get();
    }

    @Override
    protected void putSpi(String key, String value) {
        properties.put(key, value);
        try {
            flush();
        } catch (BackingStoreException e) {
            throw new FXException(MessageFormat.format(resources.getString(LC_ERROR_SAVE),
                    FilePreferences.class.getName()), e);
        }
    }

    @Override
    protected String getSpi(String key) {
        return properties.get(key);
    }

    @Override
    protected void removeSpi(String key) {
        properties.remove(key);
        try {
            flush();
        } catch (BackingStoreException e) {
            throw new FXException(MessageFormat.format(resources.getString(LC_ERROR_SAVE),
                    FilePreferences.class.getName()), e);
        }
    }

    @Override
    protected void removeNodeSpi() throws BackingStoreException {
        try {
            Files.delete(getFilePath());
        } catch (IOException e) {
            throw new BackingStoreException(new FXException(MessageFormat.format(resources.getString(LC_ERROR_SAVE),
                    FilePreferences.class.getName()), e));
        }
    }

    @Override
    protected String[] keysSpi() {
        return properties.keySet().toArray(STRINGS);
    }

    @Override
    protected String[] childrenNamesSpi() throws BackingStoreException {
        try (Stream<Path> stream = Files.find(getRootPath(), 0, (path, basicFileAttributes) ->
                path.getFileName().toString().endsWith(SUFFIX))) {
            return stream.map(path -> {
                String fileName = path.getFileName().toString();
                return fileName.substring(0, fileName.length() - SUFFIX.length());
            }).toArray(String[]::new);
        } catch (IOException e) {
            throw new BackingStoreException(new FXException(MessageFormat.format(resources.getString(LC_ERROR_LOAD),
                    FilePreferences.class.getName()), e));
        }
    }

    @Override
    protected AbstractPreferences childSpi(String name) {
        return new FilePreferences(isUserRoot, this, name);
    }

    @Override
    protected void syncSpi() throws BackingStoreException {
        Path filePath = getFilePath();
        try {
            FilePreferencesStore.load(filePath, properties);
        } catch (IOException e) {
            throw new BackingStoreException(new FXException(MessageFormat.format(resources.getString(LC_ERROR_LOAD),
                    FilePreferences.class.getName()), e));
        }
    }

    @Override
    protected void flushSpi() throws BackingStoreException {
        try {
            FilePreferencesStore.save(getFilePath(), properties, absolutePath());
        } catch (IOException e) {
            throw new BackingStoreException(new FXException(MessageFormat.format(resources.getString(LC_ERROR_LOAD),
                    FilePreferences.class.getName()), e));
        }
    }
}
