package gargoyle.fx

import gargoyle.fx.FxSplash.FxSplashNotification
import gargoyle.fx.FxUtil.get
import gargoyle.fx.log.FxLog
import javafx.application.Application
import javafx.event.EventHandler
import javafx.stage.Stage
import java.awt.Window
import kotlin.reflect.KClass

class FxWrapper : Application() {
    private lateinit var appClass: KClass<out FxApplication>
    private lateinit var appClassName: String
    private lateinit var context: FxContext

    @Suppress("UNCHECKED_CAST")
    override fun init(): Unit =
        try {
            appClassName = parameters.raw[0]
            appClass = FxIntUtil.loadClass(appClassName, javaClass.classLoader)
            context = FxContext.current
            context.register(appClassName, FxApplication::class, appClass)
            context.initializeContext(hostServices, parameters)
            FxLauncher.notifySplash(FxSplashNotification.Type.INIT, 0.0, "")
            context.getBean(appClassName, FxApplication::class).doInit()
        } catch (e: Exception) {
            FxLauncher.splashStop("")
            FxLog.error("$appClassName initialization error", e)
            throw FxException("$appClassName initialization error", e)
        }

    override fun start(primaryStage: Stage): Unit =
        try {
            context.register(Stage::class, primaryStage)
            primaryStage.properties[FxLauncher.KEY_APP] = this
            with(primaryStage) {
                onCloseRequest = EventHandler { event ->
                    try {
                        if (FxCloseFlag.values().any { FxCloseAction.KEEP == it.doIf(context) }) event.consume()
                    } catch (e: Exception) {
                        FxLog.error(e.localizedMessage, e)
                    }
                }
                FxLauncher.notifySplash(FxSplashNotification.Type.START, 0.0, "")
                FxIntUtil.prepareStage(FxUtil.resolveBaseName(appClass), this, context)
                FxUtil.loadResources(context.baseClass, context.locale, FxUtil.resolveBaseName(appClass))
                    ?.let { title = it[LC_TITLE] }
                scene = FxUtil.getOrCreateScene(context.getBean(appClassName, FxApplication::class).doStart().view)
                centerOnScreen()
                show()
            }
            FxLauncher.splashStop("")
        } catch (e: Exception) {
            FxLauncher.splashStop("$appClassName initialization error")
            throw FxException("$appClassName initialization error", e)
        }

    override fun stop() {
        try {
            askStop()
            Window.getWindows().filter { it.isDisplayable }.forEach { it.dispose() }
        } catch (e: Exception) {
            throw FxException("$appClassName initialization error", e)
        }
    }

    fun askStop() {
        try {
            context.getBean(appClassName, FxApplication::class).doStop()
        } finally {
            FxLauncher.splashStop("$appClassName initialization error")
            context.registry.close()
        }
    }

    companion object {
        private const val LC_TITLE = "title"
    }
}
