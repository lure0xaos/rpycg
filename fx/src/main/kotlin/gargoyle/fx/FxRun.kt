package gargoyle.fx

import gargoyle.fx.log.FxLog
import javafx.application.Platform
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask

object FxRun {

    fun <V> callLater(runnable: () -> V): V? {
        return if (Platform.isFxApplicationThread()) {
            try {
                CompletableFuture.supplyAsync { runnable() }.exceptionally { null }
                    .also {
                        Platform.runLater { it.join() }
                    }.get()
            } catch (e: InterruptedException) {
                FxLog.error("interrupted callLater", e)
                null
            } catch (e: ExecutionException) {
                FxLog.error("callLater", e.cause)
                null
            }
        } else {
            try {
                FutureTask(runnable).also { Platform.runLater(it) }.get()
            } catch (e: InterruptedException) {
                FxLog.error("interrupted callLater", e)
                null
            } catch (e: ExecutionException) {
                FxLog.error("callLater", e.cause)
                null
            }
        }
    }

    fun runLater(runnable: () -> Unit) {
        if (Platform.isFxApplicationThread()) CompletableFuture.runAsync { Platform.runLater(runnable) }
        else Platform.runLater(runnable)
    }

}
