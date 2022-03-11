package gargoyle.fx

import gargoyle.fx.log.FxLog
import javafx.application.Platform
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask

@Suppress("unused")
object FxRun {

    fun <V> callLater(runnable: () -> V): V? {
        return if (Platform.isFxApplicationThread()) {
            try {
                val future = CompletableFuture.supplyAsync {
                    try {
                        return@supplyAsync runnable()
                    } catch (e: Exception) {
                        throw FxException("future callLater", e)
                    }
                }.exceptionally { null }
                Platform.runLater { future.join() }
                future.get()
            } catch (e: InterruptedException) {
                FxLog.error(e, "interrupted callLater")
                null
            } catch (e: ExecutionException) {
                FxLog.error(e.cause, "callLater")
                null
            }
        } else {
            val task = FutureTask(runnable)
            Platform.runLater(task)
            try {
                task.get()
            } catch (e: InterruptedException) {
                FxLog.error(e, "interrupted callLater")
                null
            } catch (e: ExecutionException) {
                FxLog.error(e.cause, "callLater")
                null
            }
        }
    }

    fun runLater(runnable: Runnable?) {
        if (Platform.isFxApplicationThread()) {
            CompletableFuture.runAsync { Platform.runLater(runnable) }
        } else {
            Platform.runLater(runnable)
        }
    }

}
