package gargoyle.rpycg.fx;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public final class FXUtil {

    private static final Pattern PATTERN_FORMAT = Pattern.compile("#\\{([^}]+)}");

    private FXUtil() {
        throw new IllegalStateException(FXUtil.class.getName());
    }

    public static Optional<Scene> findScene(Node node) {
        return Optional.ofNullable(node).map(Node::getScene);
    }

    public static Optional<Stage> findStage(Node node) {
        return Optional.ofNullable(node).map(Node::getScene)
                .map(Scene::getWindow)
                .filter(Stage.class::isInstance)
                .map(Stage.class::cast);
    }

    public static List<String> format(List<String> formats, Object... values) {
        return format(formats, mapOf(values));
    }

    public static List<String> format(List<String> formats, Map<String, ?> values) {
        List<String> list = new ArrayList<>(formats.size());
        for (String format : formats) {
            list.add(format(format, values));
        }
        return list;
    }

    public static Map<String, String> mapOf(String... values) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0, valuesLength = values.length; i < valuesLength; i += 2) {
            map.put(values[i], values[i + 1]);
        }
        return Collections.unmodifiableMap(map);
    }

    public static String format(String format, Map<String, ?> values) {
        return PATTERN_FORMAT.matcher(format).replaceAll(match -> {
            Object value = values.get(match.group(1));
            return value != null ? value.toString() : match.group(0);
        });
    }

    public static String format(String format, Object... values) {
        return format(format, mapOf(values));
    }

    public static Scene getOrCreateScene(Parent parent) {
        return Optional.of(parent).map(Node::getScene).orElseGet(() -> new Scene(parent));
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> mapOf(Object... values) {
        Map<K, V> map = new HashMap<>();
        for (int i = 0, valuesLength = values.length; i < valuesLength; i += 2) {
            map.put((K) values[i], (V) values[i + 1]);
        }
        return Collections.unmodifiableMap(map);
    }

    public static <T> T requireNonNull(T obj, Supplier<String> messageSupplier) {
        if (obj == null) {
            throw new FXException(messageSupplier == null ? "" : messageSupplier.get());
        }
        return obj;
    }

    public static <T> T requireNonNull(T obj, String messageKey, String... args) {
        if (obj == null) {
            throw new FXUserException(messageKey, args);
        }
        return obj;
    }

    public static String stringStackTrace(Throwable e) {
        try (StringWriter stringWriter = new StringWriter(); PrintWriter printWriter = new PrintWriter(stringWriter)) {
            e.printStackTrace(printWriter);
            return stringWriter.toString();
        } catch (IOException exception) {
            return e.getLocalizedMessage();
        }
    }
}
