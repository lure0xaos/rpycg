package gargoyle.rpycg.fx;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.function.Supplier;

public final class FXUtil {
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

    public static Scene getOrCreateScene(Parent parent) {
        return Optional.of(parent).map(Node::getScene).orElseGet(() -> new Scene(parent));
    }

    public static <T> T requireNonNull(T obj, Supplier<String> messageSupplier) {
        if (obj == null) {
            throw new FXException(messageSupplier == null ? "" : messageSupplier.get());
        }
        return obj;
    }

    public static <T> T requireNonNull(T obj,
                                       String messageKey, String... args) {
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
