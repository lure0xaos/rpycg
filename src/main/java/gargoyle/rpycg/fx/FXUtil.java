package gargoyle.rpycg.fx;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
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

    public static List<String> format(List<String> formats, Map<String, Object> values) {
        List<String> list = new ArrayList<>(formats.size());
        for (String format : formats) {
            list.add(format(format, values));
        }
        return list;
    }

    public static String format(String format, Map<String, ?> values) {
        return PATTERN_FORMAT.matcher(format).replaceAll(match -> {
            Object value = values.get(match.group(1));
            return value != null ? value.toString() : match.group(0);
        });
    }

    public static Scene getOrCreateScene(Parent parent) {
        return Optional.of(parent).map(Node::getScene).orElseGet(() -> new Scene(parent));
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
