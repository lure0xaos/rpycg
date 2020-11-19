package gargoyle.rpycg.fx;

import javafx.application.Platform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public final class FXRun {
    private static final Logger log = LoggerFactory.getLogger(FXRun.class);

    public FXRun() {
        throw new IllegalStateException(FXRun.class.getName());
    }

    @Nullable
    public static <V> V callLater(@NotNull Callable<V> runnable) {
        if (Platform.isFxApplicationThread()) {
            try {
                CompletableFuture<V> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        return runnable.call();
                    } catch (Exception e) {
                        throw new RuntimeException(e.getLocalizedMessage(), e);
                    }
                }).exceptionally(e -> null);
                Platform.runLater(future::join);
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error(e.getLocalizedMessage(), e);
                return null;
            }
        } else {
            final FutureTask<V> task = new FutureTask<>(runnable);
            Platform.runLater(task);
            try {
                return task.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error(e.getLocalizedMessage(), e);
                return null;
            }
        }
    }

    public static void runLater(@NotNull Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            CompletableFuture.runAsync(() -> Platform.runLater(runnable));
        } else {
            Platform.runLater(runnable);
        }
    }
}
