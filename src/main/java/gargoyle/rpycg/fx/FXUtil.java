package gargoyle.rpycg.fx;

import gargoyle.rpycg.fx.log.FXLog;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public final class FXUtil {

    private static final String MSG_NO_STRING = "{} not found in resources of {}";
    private static final Pattern PATTERN_FORMAT = Pattern.compile("#\\{([^}]+)}");

    private FXUtil() {
        throw new IllegalStateException(FXUtil.class.getName());
    }

    public static Optional<Scene> findScene(final Node node) {
        return Optional.ofNullable(node).map(Node::getScene);
    }

    public static Optional<Scene> findScene(final Window node) {
        return Optional.ofNullable(node).map(Window::getScene);
    }

    public static Optional<Stage> findStage(final Node node) {
        return Optional.ofNullable(node).map(Node::getScene)
                .map(Scene::getWindow)
                .filter(Stage.class::isInstance)
                .map(Stage.class::cast);
    }

    public static List<String> format(final List<String> formats, final Object... values) {
        return format(formats, mapOf(values));
    }

    public static List<String> format(final List<String> formats, final Map<String, ?> values) {
        final List<String> list = new ArrayList<>(formats.size());
        for (final String format : formats) {
            list.add(format(format, values));
        }
        return list;
    }

    public static String format(final String format, final Map<String, ?> values) {
        return PATTERN_FORMAT.matcher(format).replaceAll(match -> {
            final Object value = values.get(match.group(1));
            return null != value ? value.toString() : match.group(0);
        });
    }

    public static String format(final String format, final Object... values) {
        return format(format, mapOf(values));
    }

    public static Scene getOrCreateScene(final Parent parent) {
        return Optional.of(parent).map(Node::getScene).orElseGet(() -> new Scene(parent));
    }

    public static Map<String, String> mapOf(final String... values) {
        final Map<String, String> map = new HashMap<>(values.length / 2);
        for (int i = 0, valuesLength = values.length; i < valuesLength; i += 2) {
            map.put(values[i], values[i + 1]);
        }
        return Collections.unmodifiableMap(map);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> mapOf(final Object... values) {
        final Map<K, V> map = new HashMap<>(values.length / 2);
        for (int i = 0, valuesLength = values.length; i < valuesLength; i += 2) {
            map.put((K) values[i], (V) values[i + 1]);
        }
        return Collections.unmodifiableMap(map);
    }

    public static String message(final Class<?> baseClass, final Locale locale, final String code, final String... args) {
        return message(baseClass, locale, () -> {
            FXLog.error(MSG_NO_STRING, code, baseClass);
            return MessageFormat.format(code, (Object[]) args);
        }, code, args);
    }

    public static String message(final Class<?> baseClass, final Locale locale, final Supplier<String> defaultMessage,
                                 final String code, final String... args) {
        return FXIntUtil.loadResources(baseClass, locale, resolveBaseName(baseClass))
                .filter(resourceBundle -> resourceBundle.containsKey(code))
                .map(resourceBundle -> resourceBundle.getString(code))
                .map(s -> MessageFormat.format(s, (Object[]) args))
                .orElseGet(defaultMessage);
    }

    public static <T> T requireNonNull(final T obj, final Supplier<String> messageSupplier) {
        if (null == obj) {
            throw new FXException(null == messageSupplier ? "" : messageSupplier.get());
        }
        return obj;
    }

    public static <T> T requireNonNull(final T obj, final String messageKey, final String... args) {
        if (null == obj) {
            throw new FXUserException(messageKey, args);
        }
        return obj;
    }

    public static String resolveBaseName(final FXComponent<?, ?> aClass, final String baseName) {
        return resolveBaseName(aClass.getBaseName(), baseName);
    }

    public static String resolveBaseName(final FXComponent<?, ?> aClass) {
        return aClass.getBaseName();
    }

    public static String resolveBaseName(final Class<?> aClass) {
        return resolveBaseName(aClass, aClass.getSimpleName());
    }

    public static String resolveBaseName(final Class<?> aClass, final String baseName) {
        return baseName.isEmpty() ? "" : '/' == baseName.charAt(0) ? baseName.substring(1)
                : aClass.getPackage().getName().replace('.', '/') + '/' + baseName;
    }

    public static String resolveBaseName(final String baseName, final String name) {
        return baseName.isEmpty() || name.isEmpty() ? "" : '/' == name.charAt(0) ? name.substring(1) :
                baseName.substring(0, baseName.lastIndexOf('/')) + '/' + name;
    }

    public static String stringStackTrace(final Throwable e) {
        try (final StringWriter stringWriter = new StringWriter();
             final PrintWriter printWriter = new PrintWriter(stringWriter)) {
            e.printStackTrace(printWriter);
            return stringWriter.toString();
        } catch (final IOException exception) {
            return e.getLocalizedMessage();
        }
    }
}
