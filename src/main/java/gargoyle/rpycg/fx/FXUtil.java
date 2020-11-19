package gargoyle.rpycg.fx;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.function.Supplier;

public final class FXUtil {
    private FXUtil() {
        throw new IllegalStateException(FXUtil.class.getName());
    }

    @NotNull
    public static Optional<Scene> findScene(@Nullable Node node) {
        return Optional.ofNullable(node).map(Node::getScene);
    }

    @NotNull
    public static Optional<Stage> findStage(@Nullable Node node) {
        return Optional.ofNullable(node).map(Node::getScene)
                .map(Scene::getWindow)
                .filter(Stage.class::isInstance)
                .map(Stage.class::cast);
    }

    @NotNull
    public static Scene getOrCreateScene(@NotNull Parent parent) {
        return Optional.of(parent).map(Node::getScene).orElseGet(() -> new Scene(parent));
    }

    @NotNull
    public static <T> T requireNonNull(@Nullable T obj, @Nullable Supplier<String> messageSupplier) {
        if (obj == null) {
            throw new FXException(messageSupplier == null ? "" : messageSupplier.get());
        }
        return obj;
    }

    @NotNull
    public static <T> T requireNonNull(@Nullable T obj,
                                       @PropertyKey(resourceBundle = "gargoyle.rpycg.fx.FXUserException")
                                               String messageKey, @NotNull String... args) {
        if (obj == null) {
            throw new FXUserException(messageKey, args);
        }
        return obj;
    }

    @NotNull
    public static String stringStackTrace(@NotNull Throwable e) {
        try (StringWriter stringWriter = new StringWriter(); PrintWriter printWriter = new PrintWriter(stringWriter)) {
            e.printStackTrace(printWriter);
            return stringWriter.toString();
        } catch (IOException exception) {
            return e.getLocalizedMessage();
        }
    }
}
