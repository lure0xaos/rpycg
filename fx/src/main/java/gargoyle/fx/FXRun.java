package gargoyle.fx;

import gargoyle.fx.log.FXLog;
import javafx.application.Platform;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@SuppressWarnings("unused")
public final class FXRun {

    private FXRun() {
        throw new IllegalStateException(FXRun.class.getName());
    }

    public static <V> V callLater(final Callable<V> runnable) {
        if (Platform.isFxApplicationThread()) {
            try {
                final CompletableFuture<V> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        return runnable.call();
                    } catch (final Exception e) {
                        throw new FXException("future callLater", e);
                    }
                }).exceptionally(e -> null);
                Platform.runLater(future::join);
                return future.get();
            } catch (final InterruptedException e) {
                FXLog.error(e, "interrupted callLater");
                return null;
            } catch (final ExecutionException e) {
                FXLog.error(e.getCause(), "callLater");
                return null;
            }
        } else {
            final FutureTask<V> task = new FutureTask<>(runnable);
            Platform.runLater(task);
            try {
                return task.get();
            } catch (final InterruptedException e) {
                FXLog.error(e, "interrupted callLater");
                return null;
            } catch (final ExecutionException e) {
                FXLog.error(e.getCause(), "callLater");
                return null;
            }
        }
    }

    public static void runLater(final Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            CompletableFuture.runAsync(() -> Platform.runLater(runnable));
        } else {
            Platform.runLater(runnable);
        }
    }
}
